package de.mischmaschine.database.redis

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import io.github.reactivecircus.cache4k.Cache
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.logging.Level
import kotlin.time.Duration.Companion.minutes

/**
 * ## AbstractRedis
 * This class is the base class for all Redis implementations.
 * It provides the basic functionality to connect to a Redis server and to execute commands/listen to channels (pubSub).
 * It is not intended to be used directly, but rather as a base class for concrete implementations.
 */
abstract class AbstractRedis(database: Int, logging: Boolean, ssl: Boolean) : Database {

    private val client: RedisClient
    private val functions = mutableMapOf<String, (String, String) -> Unit>()
    val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }
    val redisCacheMap = Cache.Builder()
        .maximumCacheSize(100)
        .expireAfterWrite(30.minutes)
        .build<String, Any>()

    val executor: ExecutorService = Executors.newCachedThreadPool()

    init {

        val host = Configuration.getHost(AbstractRedis::class)
        val port = Configuration.getPort(AbstractRedis::class)
        val password = Configuration.getPassword(AbstractRedis::class)

        require(host.isNotEmpty()) { "No host specified for Redis database $database" }
        require(port != 0) { "No port specified for Redis database $database" }

        this.client = when (password.isEmpty()) {
            true -> {
                RedisClient.create(RedisURI.Builder.redis(host, port).withDatabase(database).build())
            }

            false -> {
                RedisClient.create(
                    RedisURI.Builder.redis(host, port).withPassword(password.toCharArray()).withDatabase(database)
                        .withSsl(ssl)
                        .build()
                )
            }
        }

        if (!logging) this.logger.level = Level.OFF

    }

    /**
     * Updates the value of the given key asynchronously.
     *
     * @param key The key to update.
     * @param data The data to update the key with.
     */
    inline fun <reified T> updateKeyAsync(key: String, data: T): FutureAction<Unit> {
        return FutureAction {
            this.completeAsync {
                updateKeySync(key, data)
            }
        }
    }

    /**
     * Updates the value of the given key synchronously.
     *
     * @param key The key to update.
     * @param data The data to update the key with.
     */
    inline fun <reified T> updateKeySync(key: String, data: T) {
        this.redisCacheMap.invalidate(key)
        this.redisCacheMap.put(key, data as Any)
        this.logger.log(Level.INFO, "Updated key $key with data $data")
        val connection = getNewConnection()
        when (data is String || data is Number || data is Boolean) {
            true -> connection.sync().set(key, data.toString())
            false -> connection.sync().set(key, json.encodeToString(data))
        }
        connection.closeAsync()
    }

    /**
     * Gets the value of the given key synchronously.
     *
     * @param key The key to get the value of.
     *
     * @return the value of the given key, or null if the key does not exist.
     */
    inline fun <reified T> getValueSync(key: String): T? {
        return try {
            val mapValue = redisCacheMap.get(key)
            if (mapValue != null) {
                this.logger.log(Level.INFO, "Got value $mapValue from cache for key $key")
                return mapValue as T
            }
            val connection = getNewConnection()
            val result = connection.sync().get(key)
            val value = if (T::class != String::class) {
                json.decodeFromString(result) as T
            } else {
                result as T
            }
            connection.closeAsync()
            this.redisCacheMap.put(key, value as Any)
            return value
        } catch (e: NullPointerException) {
            null
        }
    }

    /**
     * Gets the value of the given key asynchronously.
     *
     * @param key The key to get the value of.
     *
     * @return the value of the given key, or null if the key does not exist.
     */
    inline fun <reified T> getValueAsync(key: String): FutureAction<T> {
        return FutureAction {
            executor.submit {
                val result = getValueSync<T>(key)
                if (result != null) {
                    this.complete(result)
                } else {
                    this.completeExceptionally(NullPointerException("Key $key does not exist."))
                }
            }
        }
    }


    /**
     * Deletes the given key synchronously.
     * @param key The key to delete.
     * @see [redisSync]
     */
    fun deleteKeySync(vararg key: String) {
        key.forEach {
            this.redisCacheMap.invalidate(it)
        }
        getNewConnection().let {
            it.sync().del(*key)
            it.closeAsync()
        }
    }

    /**
     * Deletes the given key asynchronously.
     * @param key The key to delete.
     *
     * @see [redisAsync]
     */
    fun deleteKeyAsync(vararg key: String): FutureAction<Unit> {
        return FutureAction {
            this.completeAsync {
                deleteKeySync(*key)
            }
        }
    }

    /**
     * Subscribes to the given channel. The given function will be called when a message is received.
     *
     * @param channel The channel to subscribe to.
     * @param function The function to call when a message is received.
     */
    fun subscribe(channel: String, function: (String, String) -> Unit) {
        val connection = getClient().connectPubSub()
        connection.addListener(Listener())
        connection.async().subscribe(channel)
        functions[channel] = function
    }

    /**
     * Unsubscribes the given channel.
     *
     * @param channel The channel to unsubscribe from.
     */
    fun unsubscribe(vararg channel: String) {
        val pubSubConnection = client.connectPubSub()
        pubSubConnection.async().unsubscribe(*channel).thenAccept {
            pubSubConnection.closeAsync()
        }
        logger.info("Unsubscribed from ${channel.joinToString(", ")}")
    }

    /**
     * This method publishes the given message to the given channel.
     *
     * @param channel The channel to publish to.
     * @param message The message to publish.
     */
    inline fun <reified T> publish(channel: String, message: T): FutureAction<Unit> {
        return FutureAction {
            val pubSubConnection = getClient().connectPubSub()
            when (message is String || message is Number || message is Boolean) {
                true -> pubSubConnection.async().publish(channel, message.toString()).thenAccept {
                    pubSubConnection.closeAsync()
                    this.complete(Unit)
                }

                false -> pubSubConnection.async().publish(channel, this@AbstractRedis.json.encodeToString(message))
                    .thenAccept {
                        pubSubConnection.closeAsync()
                        this.complete(Unit)
                    }
            }
            logger.info("Published to channel '$channel': '$message'")
        }
    }


    fun getNewConnection(): StatefulRedisConnection<String, String> {
        return client.connect()
    }

    fun shutdown() {
        client.shutdown()
    }

    fun getClient() = this.client

    private inner class Listener : RedisPubSubAdapter<String, String>() {

        override fun message(channel: String, message: String) =
            functions[channel]?.invoke(channel, message) ?: let {
                logger.log(
                    Level.WARNING,
                    "Received message on channel '$channel' but no function was registered for this channel."
                )
                unsubscribe(channel)
            }
    }
}
