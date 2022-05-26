package de.mischmaschine.database.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import java.util.concurrent.CompletableFuture

abstract class AbstractMongoDB(
    host: String,
    port: Int,
    username: String,
    password: String,
    collectionName: String,
) {
    private val mongoClient: MongoClient?
    private val mongoDatabase: MongoDatabase?
    private val identifier = "uniqueId_key"

    init {
        if (host.isEmpty()) throw NullPointerException("Host is empty")
        if (collectionName.isEmpty()) throw NullPointerException("CollectionName is empty")
        val uri = if (username.isEmpty() && password.isEmpty()) {
            "mongodb://$host:$port/?authSource=$collectionName"
        } else {
            "mongodb://$username:$password@$host:$port/?authSource=$collectionName"
        }
        val connectionString = ConnectionString(uri)
        val settings: MongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        this.mongoClient = MongoClients.create(settings)
        this.mongoDatabase = this.mongoClient.getDatabase(collectionName)
    }

    fun close() {
        this.mongoClient?.close()
    }

    fun getDocumentAsync(collection: String, key: String): CompletableFuture<Document> {
        return CompletableFuture.supplyAsync {
            getDocumentSync(collection, key)
        }
    }

    fun getDocumentSync(collection: String, key: String): Document? {
        return mongoDatabase?.getCollection(collection)?.find()?.filter(Filters.eq(identifier, key))?.first()
    }

    fun getAllDocumentsAsync(collection: String): CompletableFuture<List<Document>> {
        return CompletableFuture.supplyAsync {
            getAllDocumentsSync(collection)
        }
    }

    fun getAllDocumentsSync(collection: String): List<Document> {
        val list = mutableListOf<Document>()
        val findIterable = mongoDatabase?.getCollection(collection)?.find()
        findIterable?.let {
            it.forEach { document ->
                list.add(document)
            }
        }
        return list
    }

    fun insertAsync(collection: String, key: String, document: Document) {
        CompletableFuture.runAsync {
            insertSync(collection, key, document)
        }.get()
    }

    fun insertSync(collection: String, key: String, document: Document) {
        document[identifier] = key
        mongoDatabase?.getCollection(collection)?.insertOne(document)
            ?: throw NullPointerException("MongoDataBase or collection is null")
    }

    fun updateAsync(collection: String, key: String, document: Document) {
        CompletableFuture.runAsync {
            updateSync(collection, key, document)
        }.get()
    }

    fun updateSync(collection: String, key: String, document: Document) {
        document[identifier] = key
        val mongoCollection =
            this.mongoDatabase?.getCollection(collection) ?: throw NullPointerException("MongoDataBase is null")
        val first = mongoCollection.find(Filters.eq(identifier, key)).first()
        first?.let { mongoCollection.replaceOne(it, document) }
    }

    fun deleteAsync(collection: String, key: String) {
        CompletableFuture.runAsync {
            deleteSync(collection, key)
        }.get()
    }

    fun deleteSync(collection: String, key: String) {
        CompletableFuture.runAsync {
            val first = this.mongoDatabase?.getCollection(collection)?.find(Filters.eq(identifier, key))?.first()
                ?: return@runAsync
            this.mongoDatabase.getCollection(collection).deleteOne(first)
        }.get()
    }

    fun existAsync(collection: String, key: String): Boolean {
        return getDocumentSync(collection, key) != null
    }

    fun existSync(collection: String, key: String): Boolean {
        return getDocumentAsync(collection, key).get() != null
    }
}