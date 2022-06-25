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

    fun close() {
        this.mongoClient.close()
    }

    fun getDocumentAsync(collection: String, key: String): CompletableFuture<Document?> {
        return CompletableFuture.supplyAsync {
            getDocumentSync(collection, key)
        }
    }

    fun getDocumentSync(collection: String, key: String): Document? {
        return mongoDatabase.getCollection(collection).find().filter(Filters.eq(identifier, key)).first()
    }

    fun getAllDocumentsAsync(collection: String): CompletableFuture<List<Document>> {
        return CompletableFuture.supplyAsync {
            getAllDocumentsSync(collection)
        }
    }

    fun getAllDocumentsSync(collection: String): List<Document> {
        val list = mutableListOf<Document>()
        val findIterable = mongoDatabase.getCollection(collection).find()
        findIterable.forEach {
            list.add(it)
        }
        return list
    }

    fun countDocumentsSync(collection: String): Long {
        return mongoDatabase.getCollection(collection).countDocuments()
    }

    fun countDocumentsAsync(collection: String): CompletableFuture<Long> {
        return CompletableFuture.supplyAsync {
            countDocumentsSync(collection)
        }
    }

    fun insertDocumentAsync(collection: String, key: String, document: Document) {
        CompletableFuture.runAsync {
            insertDocumentSync(collection, key, document)
        }
    }

    fun insertDocumentSync(collection: String, key: String, document: Document) {
        document[identifier] = key
        mongoDatabase.getCollection(collection).insertOne(document)
    }

    fun insertDocumentCollectionSync(collection: String, key: String, documents: Collection<Document>) {
        documents.filter { documents.isNotEmpty() }.forEach { it[identifier] = key }
        mongoDatabase.getCollection(collection).insertMany(documents.toList())
    }


    fun insertDocumentCollectionAsync(collection: String, key: String, documents: Collection<Document>) {
        CompletableFuture.runAsync {
            insertDocumentCollectionSync(collection, key, documents)
        }
    }

    fun bulkWriteAsync(
        collection: String,
        writeModelList: List<WriteModel<Document>>
    ): CompletableFuture<BulkWriteResult> {
        return CompletableFuture.supplyAsync {
            bulkWriteSync(collection, writeModelList)
        }
    }

    fun bulkWriteSync(collection: String, writeModelList: List<WriteModel<Document>>): BulkWriteResult {
        return mongoDatabase.getCollection(collection).bulkWrite(writeModelList)
    }

    fun deleteManySync(collection: String, key: String) {
        mongoDatabase.getCollection(collection).deleteMany(Filters.eq(identifier, key))
    }

    fun deleteManyAsync(collection: String, key: String) {
        CompletableFuture.runAsync {
            deleteManySync(collection, key)
        }
    }

    fun renameCollectionSync(collection: String, newCollectionName: String) {
        mongoDatabase.getCollection(collection).renameCollection(MongoNamespace(newCollectionName))
    }

    fun renameCollectionAsync(collection: String, newCollectionName: String) {
        CompletableFuture.runAsync {
            renameCollectionSync(collection, newCollectionName)
        }
    }

    fun dropCollectionSync(collection: String) {
        mongoDatabase.getCollection(collection).drop()
    }

    fun dropCollectionAsync(collection: String) {
        CompletableFuture.runAsync {
            dropCollectionSync(collection)
        }
    }

    fun updateDocumentAsync(collection: String, key: String, document: Document) {
        CompletableFuture.runAsync {
            updateDocumentSync(collection, key, document)
        }
    }

    fun updateDocumentSync(collection: String, key: String, document: Document) {
        document[identifier] = key
        val mongoCollection =
            this.mongoDatabase.getCollection(collection)
        val first = mongoCollection.find(Filters.eq(identifier, key)).first()
        first?.let { mongoCollection.replaceOne(it, document) }
    }

    fun deleteDocumentAsync(collection: String, key: String) {
        CompletableFuture.runAsync {
            deleteDocumentSync(collection, key)
        }
    }

    fun deleteDocumentSync(collection: String, key: String) {
        CompletableFuture.runAsync {
            val first =
                mongoDatabase.getCollection(collection).find(Filters.eq(identifier, key)).first() ?: return@runAsync
            mongoDatabase.getCollection(collection).deleteOne(first)
        }
    }

    fun getMongoDatabase(): MongoDatabase {
        return this.mongoDatabase
    }

    fun getMongoClient(): MongoClient {
        return this.mongoClient
    }

    fun exist(collection: String, key: String): Boolean {
        return getDocumentSync(collection, key) != null
    }
}