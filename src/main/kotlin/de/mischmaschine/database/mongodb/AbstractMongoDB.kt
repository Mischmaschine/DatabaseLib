package de.mischmaschine.database.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoNamespace
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.WriteModel
import de.mischmaschine.database.mongodb.configuration.MongoConfiguration
import org.bson.Document
import java.util.concurrent.CompletableFuture

abstract class AbstractMongoDB(
    dataBaseName: String,
) {
    private val mongoClient: MongoClient
    private val mongoDatabase: MongoDatabase
    private val identifier = "uniqueId_key"

    init {
        val host = MongoConfiguration.getHost() ?: ""
        val username = MongoConfiguration.getUsername() ?: ""
        val password = MongoConfiguration.getPassword() ?: ""
        val port = MongoConfiguration.getPort() ?: "27017"

        if (host.isEmpty()) throw IllegalArgumentException("Host is empty")
        if (dataBaseName.isEmpty()) throw IllegalArgumentException("CollectionName is empty")
        val uri = if (username.isEmpty() && password.isEmpty()) {
            "mongodb://$host:$port/?authSource=$dataBaseName"
        } else {
            "mongodb://$username:$password@$host:$port/?authSource=$dataBaseName"
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
     */
    fun close() {
        this.mongoClient.close()
    }

    fun getDocumentAsync(collection: String, key: String): CompletableFuture<Document?> {
        return CompletableFuture.supplyAsync {
            getDocumentSync(collection, key)
        }
    }

    /**
     * @param collection The collection to get the document from.
     * @param key The key of the document.
     * Returns the document with the given key.
     */
    fun getDocumentSync(collection: String, key: String): Document? {
        return mongoDatabase.getCollection(collection).find().filter(Filters.eq(identifier, key)).first()
    }


    /**
     * @param collection The collection to get the document from.
     *
     * @return A list of documents with the given key.
     */
    fun getAllDocumentsSync(collection: String): List<Document> {
        return mongoDatabase.getCollection(collection).find().toList()
    }

    fun getAllDocumentsAsync(collection: String): CompletableFuture<List<Document>> {
        return CompletableFuture.supplyAsync {
            getAllDocumentsSync(collection)
        }
    }

    /**
     * @param collection The collection to count the documents.
     * Counts the number of documents in the collection.
     */
    fun countDocumentsSync(collection: String): Long {
        return mongoDatabase.getCollection(collection).countDocuments()
    }

    fun countDocumentsAsync(collection: String): CompletableFuture<Long> {
        return CompletableFuture.supplyAsync {
            countDocumentsSync(collection)
        }
    }

    /**
     * @param collection The collection to insert the document into.
     * @param key The key of the document.
     *
     * Inserts a document into the collection.
     */
    fun insertDocumentSync(collection: String, key: String, document: Document) {
        document[identifier] = key
        mongoDatabase.getCollection(collection).insertOne(document)
    }

    fun insertDocumentAsync(collection: String, key: String, document: Document) {
        CompletableFuture.runAsync {
            insertDocumentSync(collection, key, document)
        }
    }

    /**
     * @param collection The collection to insert the documents into.
     * @param documents The documents to insert.
     *
     * Inserts a Collection of documents into the given collection.
     */
    fun insertDocumentCollectionSync(collection: String, key: String, documents: Collection<Document>) {
        documents.filter { documents.isNotEmpty() }.forEach { it[identifier] = key }
        mongoDatabase.getCollection(collection).insertMany(documents.toList())
    }

    fun insertDocumentCollectionAsync(collection: String, key: String, documents: Collection<Document>) {
        CompletableFuture.runAsync {
            insertDocumentCollectionSync(collection, key, documents)
        }
    }

    /**
     * @param collection The collection to insert the documents into.
     * @param writeModelList The list of write models to insert.
     *
     * @return The result of the bulk insert.
     */
    fun bulkWriteSync(collection: String, writeModelList: List<WriteModel<Document>>): BulkWriteResult {
        return mongoDatabase.getCollection(collection).bulkWrite(writeModelList)
    }

    fun bulkWriteAsync(
        collection: String,
        writeModelList: List<WriteModel<Document>>
    ): CompletableFuture<BulkWriteResult> {
        return CompletableFuture.supplyAsync {
            bulkWriteSync(collection, writeModelList)
        }
    }

    /**
     * Deletes all documents in the collection which equal the key.
     */
    fun deleteManySync(collection: String, key: String) {
        mongoDatabase.getCollection(collection).deleteMany(Filters.eq(identifier, key))
    }

    fun deleteManyAsync(collection: String, key: String) {
        CompletableFuture.runAsync {
            deleteManySync(collection, key)
        }
    }

    /**
     * @param collection The collection which should be renamed.
     *
     * Renames a collection.
     */
    fun renameCollectionSync(collection: String, newCollectionName: String) {
        mongoDatabase.getCollection(collection).renameCollection(MongoNamespace(newCollectionName))
    }

    fun renameCollectionAsync(collection: String, newCollectionName: String) {
        CompletableFuture.runAsync {
            renameCollectionSync(collection, newCollectionName)
        }
    }

    /**
     * @param collection The collection which should be dropped.
     */
    fun dropCollectionSync(collection: String) {
        mongoDatabase.getCollection(collection).drop()
    }

    fun dropCollectionAsync(collection: String) {
        CompletableFuture.runAsync {
            dropCollectionSync(collection)
        }
    }

    /**
     * @param collection The collection to update the document in.
     * @param key The key of the document.
     * @param document The document to update.
     *
     * Updates a document in the collection.
     */
    fun updateDocumentSync(collection: String, key: String, document: Document) {
        document[identifier] = key
        this.mongoDatabase.getCollection(collection).findOneAndReplace(Filters.eq(identifier, key), document)
    }

    fun updateDocumentAsync(collection: String, key: String, document: Document) {
        CompletableFuture.runAsync {
            updateDocumentSync(collection, key, document)
        }
    }

    /**
     * @param collection The collection to delete the document from.
     * @param key The key of the document.
     *
     * Deletes a document from the collection.
     *
     * @return The result of the deletion.
     */
    fun deleteDocumentSync(collection: String, key: String): Document? {
        return mongoDatabase.getCollection(collection).findOneAndDelete(Filters.eq(identifier, key))
    }

    fun deleteDocumentAsync(collection: String, key: String): CompletableFuture<Document?> {
        return CompletableFuture.supplyAsync {
            deleteDocumentSync(collection, key)
        }
    }

    /**
     * @return the MongoDatabase
     */
    fun getMongoDatabase(): MongoDatabase {
        return this.mongoDatabase
    }

    /**
     * @return the MongoClient
     */
    fun getMongoClient(): MongoClient {
        return this.mongoClient
    }

    /**
     * @return returns true if document exist else false
     */
    fun existSync(collection: String, key: String): Boolean {
        return getDocumentSync(collection, key) != null
    }

    fun existAsync(collection: String, key: String): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync { getDocumentSync(collection, key) != null }
    }
}