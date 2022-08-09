package de.mischmaschine.database.redis

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.logging.Level

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
    private val pubSub: StatefulRedisPubSubConnection<String, String>

    init {

        val host = Configuration.getHost(AbstractRedis::class)
        val port = Configuration.getPort(AbstractRedis::class)
        val password = Configuration.getPassword(AbstractRedis::class)

        if (host.isEmpty()) throw IllegalArgumentException("No host specified for Redis database $database")
        if (port == 0) throw IllegalArgumentException("No port specified for Redis database $database")

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
        }.also {
            this.pubSub = it.connectPubSub().also { it.addListener(Listener()) }
        }

        if (!logging) this.logger.level = Level.OFF

    }

    /**
     * Updates the value of the given key asynchronously.
     *
     * @param key The key to update.
     * @param data The data to update the key with.
     */
    inline fun <reified T> updateKeyAsync(key: String, data: T) {
        val connection = getNewConnection()
        when (data is String || data is Number || data is Boolean) {
            true -> connection.async().set(key, data.toString()).thenAccept {
                connection.closeAsync()
            }

            false -> connection.async().set(key, json.encodeToString(data)).thenAccept {
                connection.closeAsync()
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
        val connection = getNewConnection()
        var value = connection.sync().get(key) as T
        if (T::class != String::class) {
            value = json.decodeFromString(value.toString()) as T
        }
        connection.closeAsync()
        return value
    }

    /**
     * Gets the value of the given key asynchronously.
     *
     * @param key The key to get the value of.
     *
     * @return the value of the given key, or null if the key does not exist.
     */
    inline fun <reified T> getValueAsync(key: String): FutureAction<T> = FutureAction {
        val connection = getNewConnection()
        connection.async().get(key).whenComplete { result, throwable ->
            throwable?.let {
                this.completeExceptionally(throwable)
            } ?: result?.let {
                if (T::class == String::class) {
                    this.complete(it as T)
                } else {
                    this.complete(json.decodeFromString<T>(it))
                }
            } ?: this.completeExceptionally(NullPointerException("No result found for key $key"))
            connection.closeAsync().also {
                println("Closed connection")
            }
        }
    }

    /**
     * Deletes the given key synchronously.
     * @param key The key to delete.
     * @see [redisSync]
     */
    fun deleteKeySync(vararg key: String) {
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
    fun deleteKeyAsync(vararg key: String) {
        val connection = getNewConnection()
        connection.async().del(*key).thenAccept {
            connection.closeAsync()
        }
    }

    /**
     * Subscribes to the given channel. The given function will be called when a message is received.
     *
     * @param channel The channel to subscribe to.
     * @param function The function to call when a message is received.
     */
    fun subscribe(channel: String, type: Class<*>, function: (String, Any) -> Unit) {
        Listener(channel, type, function)
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
    inline fun <reified T> publish(channel: String, message: T) {
        val pubSubConnection = getClient().connectPubSub()
        when (message is String || message is Number || message is Boolean) {
            true -> pubSubConnection.async().publish(channel, message.toString()).thenAccept {
                pubSubConnection.closeAsync()
            }

            false -> pubSubConnection.async().publish(channel, this.json.encodeToString(message)).thenAccept {
                pubSubConnection.closeAsync()
            }
        }
        logger.info("Published to channel '$channel': '$message'")
    }


    fun getNewConnection(): StatefulRedisConnection<String, String> {
        return client.connect()
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
