package de.mischmaschine.database.mongodb

import com.mongodb.*
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.InsertOneResult
import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.redis.FutureAction
import io.github.reactivecircus.cache4k.Cache
import org.bson.Document
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.minutes

/**
 * ## AbstractMongoDB
 * This class is an abstract base class for MongoDB implementations.
 */
abstract class AbstractMongoDB(
    dataBaseName: String,
    var uri: String = "",
) : Database {
    private val mongoClient: MongoClient
    private val mongoDatabase: MongoDatabase
    private val identifier = "uniqueId_key"
    private val executor = Executors.newCachedThreadPool()
    private val documentCache = Cache.Builder<String, Document>()
        .maximumCacheSize(100)
        .expireAfterWrite(30.minutes)
        .build()

    final override var logger: Logger? = null


    init {
        val host = Configuration.getHost(AbstractMongoDB::class)
        val username = Configuration.getUsername(AbstractMongoDB::class)
        val password = Configuration.getPassword(AbstractMongoDB::class)
        val port = Configuration.getPort(AbstractMongoDB::class)

        if (host.isEmpty()) throw IllegalArgumentException("Host is empty")
        if (dataBaseName.isEmpty()) throw IllegalArgumentException("CollectionName is empty")
        if (uri.isEmpty()) {
            uri = if (username.isEmpty() && password.isEmpty()) {
                "mongodb://$host:$port/?authSource=$dataBaseName"
            } else {
                "mongodb://$username:$password@$host:$port/?authSource=$dataBaseName"
            }
        }


        val connectionString = ConnectionString(uri)
        val settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        this.mongoClient = MongoClients.create(settings)
        this.mongoDatabase = this.mongoClient.getDatabase(dataBaseName)
    }

    /**
     * Closes the connection to the database.
     * @see MongoClient
     */
    fun close() {
        this.mongoClient.close()
    }

    /**
     * @param collection The collection to get the document from.
     * @param key The key of the document.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Filters
     * @see Document
     *
     * @return The document with the given key.
     */
    fun getDocumentSync(collection: String, key: String, documentIdentifier: String = identifier): Document? {
        val value = documentCache.get(key)
        if (value != null) {
            return value
        }
        return this.getCollection(collection).find().filter(Filters.eq(documentIdentifier, key)).first()
    }

    /**
     * @param collection The collection to get the document from.
     * @param key The key of the document.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Filters
     * @see Document
     * @see FutureAction
     *
     * @return A future which will contain the document with the given key.
     */
    fun getDocumentAsync(
        collection: String,
        key: String,
        documentIdentifier: String = identifier
    ): FutureAction<Document> {
        return FutureAction {
            executor.submit {
                getDocumentSync(collection, key, documentIdentifier)?.let {
                    this.complete(it)
                } ?: this.completeExceptionally(Exception("Document not found"))
            }
        }

    }

    /**
     * This function returns a list with the documents from the collection blocking.
     * @param collection The collection to get the document from.
     *
     * @see MongoCollection
     * @see Document
     *
     * @return A list with the documents from the collection.
     */
    fun getAllDocumentsSync(collection: String): List<Document> {
        return this.getCollection(collection).find().toList()
    }

    fun getAllCachedDocuments(collection: String): List<Document> {
        return documentCache.asMap().values.toList()
    }

    /**
     * This function returns a list with the documents from the collection non-blocking.
     *
     * @param collection The collection to get the document from.
     *
     * @see MongoCollection
     * @see Document
     * @see CompletableFuture
     *
     * @return A list with the documents from the collection.
     */
    fun getAllDocumentsAsync(collection: String): FutureAction<List<Document>> {
        return FutureAction {
            executor.submit {
                this.complete(getAllDocumentsSync(collection))
            }
        }
    }

    /**
     * @param collection The collection to count the documents.
     *
     * @see MongoCollection
     *
     * @return The number of documents in the collection.
     */
    fun countDocumentsSync(collection: String): Long {
        return this.getCollection(collection).countDocuments()
    }

    /**
     * @param collection The collection to count the documents.
     *
     * @see MongoCollection
     * @see CompletableFuture
     *
     * @return A future which will contain the number of documents in the given collection.
     */
    fun countDocumentsAsync(collection: String): FutureAction<Long> {
        return FutureAction {
            executor.submit {
                this.complete(countDocumentsSync(collection))
            }
        }
    }

    /**
     * This function inserts the document blocking into the given collection.
     *
     * @param collection The collection to insert the document into.
     * @param key The key of the document.
     * @param document The document to insert.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Document
     */
    fun insertDocumentSync(
        collection: String,
        key: String,
        document: Document,
        documentIdentifier: String = identifier
    ): InsertOneResult? {
        document[documentIdentifier] = key
        return try {
            this.documentCache.put(key, document)
            this.getCollection(collection).insertOne(document)
        } catch (exception: MongoServerException) {
            null
        } catch (exception: RuntimeException) {
            null
        }
    }

    /**
     * This function inserts the document non-blocking into the given collection.
     *
     * @param collection The collection to insert the document into.
     * @param key The key of the document.
     * @param document The document to insert.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Document
     * @see CompletableFuture
     */
    fun insertDocumentAsync(
        collection: String,
        key: String,
        document: Document,
        documentIdentifier: String = identifier
    ): FutureAction<InsertOneResult> {
        return FutureAction {
            executor.submit {
                insertDocumentSync(collection, key, document, documentIdentifier)?.let {
                    this.complete(it)
                }
                    ?: this.completeExceptionally(MongoException("Could not write to database. For a more detailed message, use the sync function temporally."))
            }
        }
    }

    /**
     * This function inserts the documents blocking into the given collection.
     *
     * @param collection The collection to insert the documents into.
     * @param documents The documents to insert.
     *
     * @see MongoCollection
     * @see Document
     */
    fun insertDocumentCollectionSync(collection: String, key: String, documents: Collection<Document>) {
        documents.filter { documents.isNotEmpty() }.forEach { it[identifier] = key }
        this.getCollection(collection).insertMany(documents.toList())
    }

    /**
     * This function inserts the documents non-blocking into the given collection.
     *
     * @param collection The collection to insert the documents into.
     * @param key The key of the document.
     * @param documents The documents to insert.
     *
     * @see MongoCollection
     * @see Document
     * @see CompletableFuture
     */
    fun insertDocumentCollectionAsync(collection: String, key: String, documents: Collection<Document>) {
        CompletableFuture.runAsync {
            insertDocumentCollectionSync(collection, key, documents)
        }
    }

    /**
     * This function create a bulk write operation and inserts the documents blocking into the given collection.
     *
     * @param collection The collection to insert the documents into.
     * @param writeModelList The list of write models to insert.
     *
     * @see MongoCollection
     * @see WriteModel
     * @see BulkWriteResult
     *
     * @return The result of the bulk insert.
     */
    fun bulkWriteSync(collection: String, writeModelList: List<WriteModel<Document>>): BulkWriteResult {
        return this.getCollection(collection).bulkWrite(writeModelList)
    }

    /**
     * This function create a bulk write operation and inserts the documents non-blocking into the given collection.
     *
     * @param collection The collection to insert the documents into.
     * @param writeModelList The list of write models to insert.
     *
     * @see MongoCollection
     * @see WriteModel
     * @see BulkWriteResult
     * @see CompletableFuture
     *
     * @return The result of the bulk insert.
     */
    fun bulkWriteAsync(
        collection: String,
        writeModelList: List<WriteModel<Document>>,
    ): CompletableFuture<BulkWriteResult> {
        return CompletableFuture.supplyAsync {
            bulkWriteSync(collection, writeModelList)
        }
    }

    /**
     * This function deletes the document's blocking with the given key.
     *
     * @param collection The collection to update the document in.
     * @param key The key of the document.
     * @param documentIdentifier The identifier of the documents.
     *
     * @see MongoCollection
     * @see Filters
     */
    fun deleteManySync(collection: String, key: String, documentIdentifier: String = identifier) {
        this.documentCache.invalidate(key)
        this.getCollection(collection).deleteMany(Filters.eq(documentIdentifier, key))
    }

    /**
     * This function deletes the document's non-blocking with the given key.
     *
     * @param collection The collection to delete the document from.
     * @param key The key of the document.
     * @param documentIdentifier The identifier of the documents.
     *
     * @see MongoCollection
     * @see Filters
     * @see CompletableFuture
     */
    fun deleteManyAsync(collection: String, key: String, documentIdentifier: String = identifier) {
        CompletableFuture.runAsync {
            deleteManySync(collection, key, documentIdentifier)
        }
    }

    /**
     * This function renames the collection blocking.
     *
     * @param collection The collection which should be renamed.
     * @param newCollectionName The new name of the collection.
     *
     * @see MongoCollection
     * @see MongoNamespace
     */
    fun renameCollectionSync(collection: String, newCollectionName: String) {
        this.getCollection(collection).renameCollection(MongoNamespace(newCollectionName))
    }

    /**
     * This function renames the collection non-blocking.
     *
     * @param collection The collection which should be renamed.
     * @param newCollectionName The new name of the collection.
     *
     * @see MongoCollection
     * @see MongoNamespace
     */
    fun renameCollectionAsync(collection: String, newCollectionName: String) {
        CompletableFuture.runAsync {
            renameCollectionSync(collection, newCollectionName)
        }
    }

    /**
     * This function drops the collection blocking.
     *
     * @param collection The collection which should be dropped.
     *
     * @see MongoCollection
     */
    fun dropCollectionSync(collection: String) {
        this.getCollection(collection).drop()
    }

    /**
     * This function drops the collection non-blocking.
     *
     * @param collection The collection which should be dropped.
     *
     * @see MongoCollection
     * @see CompletableFuture
     */
    fun dropCollectionAsync(collection: String) {
        CompletableFuture.runAsync {
            dropCollectionSync(collection)
        }
    }

    /**
     * This function replaces the document blocking with the given key.
     *
     * @param collection The collection to update the document in.
     * @param key The key of the document.
     * @param document The document to update.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Document
     * @see Filters
     */
    fun replaceDocumentSync(
        collection: String,
        key: String,
        document: Document,
        documentIdentifier: String = identifier
    ): Document? {
        document[documentIdentifier] = key
        this.documentCache.invalidate(key)
        this.documentCache.put(key, document)
        return this.getCollection(collection).findOneAndReplace(Filters.eq(documentIdentifier, key), document)
    }

    /**
     * This function replaces the document non-blocking with the given key.
     *
     * @param collection The collection to update the document in.
     * @param key The key of the document.
     * @param document The document to update.
     *
     * @see MongoCollection
     * @see Document
     * @see Filters
     * @see CompletableFuture
     */
    fun replaceDocumentAsync(
        collection: String,
        key: String,
        document: Document,
        documentIdentifier: String = identifier
    ): FutureAction<Document> {
        return FutureAction {
            executor.submit {
                replaceDocumentSync(collection, key, document, documentIdentifier)?.let {
                    this.complete(it)
                } ?: this.completeExceptionally(NoSuchElementException("Document not found"))
            }
        }
    }

    /**
     * This function updates the document blocking with the given key.
     *
     * @param collection The collection to update the document in.
     * @param key The key of the document.
     * @param document The document to update.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Document
     * @see Filters
     */
    fun updateDocumentSync(
        collection: String,
        key: String,
        document: Document,
        documentIdentifier: String = identifier
    ): Document? {
        document[documentIdentifier] = key
        this.documentCache.invalidate(key)
        this.documentCache.put(key, document)
        return this.getCollection(collection)
            .findOneAndUpdate(Filters.eq(documentIdentifier, key), Document("\$set", document))
    }

    /**
     * This function updates the document non-blocking with the given key.
     *
     * @param collection The collection to update the document in.
     * @param key The key of the document.
     * @param document The document to update.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Document
     * @see Filters
     * @see CompletableFuture
     */
    fun updateDocumentAsync(
        collection: String,
        key: String,
        document: Document,
        documentIdentifier: String = identifier
    ): FutureAction<Document> {
        return FutureAction {
            executor.submit {
                updateDocumentSync(collection, key, document, documentIdentifier)?.let {
                    this.complete(it)
                } ?: this.completeExceptionally(NoSuchElementException("Document not found"))
            }
        }
    }

    /**
     * This function deletes the document blocking with the given key.
     *
     * @param collection The collection to delete the document from.
     * @param key The key of the document.
     * @param documentIdentifier The identifier of the document.
     * @return A nullable result of the deletion.
     *
     * @see MongoCollection
     * @see Filters
     * @see Document
     */
    fun deleteDocumentSync(collection: String, key: String, documentIdentifier: String = identifier): Document? {
        this.documentCache.invalidate(key)
        return this.getCollection(collection).findOneAndDelete(Filters.eq(documentIdentifier, key))
    }

    /**
     * This function deletes the document non-blocking with the given key.
     *
     * @param collection The collection to delete the document from.
     * @param key The key of the document.
     * @param documentIdentifier The identifier of the document.
     *
     * @see MongoCollection
     * @see Filters
     * @see Document
     * @see CompletableFuture
     *
     * @return A nullable result of the deletion.
     */
    fun deleteDocumentAsync(
        collection: String,
        key: String,
        documentIdentifier: String = identifier
    ): FutureAction<Document> {
        return FutureAction {
            executor.submit {
                deleteDocumentSync(collection, key, documentIdentifier)?.let {
                    this.complete(it)
                } ?: this.completeExceptionally(NoSuchElementException())
            }
        }
    }

    /**
     * @param collection The collection to get
     *
     * @see MongoCollection
     * @see Document
     *
     * @return The mongo collection.
     */
    fun getCollection(collection: String): MongoCollection<Document> {
        return this.mongoDatabase.getCollection(collection)
    }

    /**
     * @see MongoDatabase
     * @return the MongoDatabase
     */
    fun getMongoDatabase(): MongoDatabase {
        return this.mongoDatabase
    }

    /**
     * @see MongoClient
     * @return the MongoClient
     */
    fun getMongoClient(): MongoClient {
        return this.mongoClient
    }

    /**
     * This function checks if the document exist blocking.
     *
     * @param collection The collection to get
     *
     * @see MongoCollection
     * @see Document
     *
     * @return a boolean if the document exist else false
     */
    fun existSync(collection: String, key: String): Boolean {
        return getDocumentSync(collection, key) != null
    }

    /**
     * This function checks if the document exist non-blocking.

     * @param collection The collection to get
     *
     * @see MongoCollection
     * @see Document
     * @see CompletableFuture
     *
     * @return a boolean if the document exist else false
     */
    fun existAsync(collection: String, key: String): FutureAction<Boolean> {
        return FutureAction {
            executor.submit {
                this.complete(getDocumentSync(collection, key) != null)
            }
        }
    }
}