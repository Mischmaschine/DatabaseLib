package de.mischmaschine.database.redis

import com.google.gson.GsonBuilder
import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import java.util.logging.Level

/**
 * ## AbstractRedis
 * This class is the base class for all Redis implementations.
 * It provides the basic functionality to connect to a Redis server and to execute commands/listen to channels (pubSub).
 * It is not intended to be used directly, but rather as a base class for concrete implementations.
 */
abstract class AbstractRedis(database: Int, logging: Boolean) : Database {

    private val client: RedisClient
    private val connection: StatefulRedisConnection<String, String>
    private val gson = GsonBuilder().serializeNulls().create()
    private val redisSync: RedisCommands<String, String>
    private val redisAsync: RedisAsyncCommands<String, String>
    private val functions = mutableMapOf<String, (String, String) -> Unit>()
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
                        .build()
                )
            }
        }.also {
            this@AbstractRedis.connection = it.connect().also { statefulRedisConnection ->
                statefulRedisConnection.run {
                    this@AbstractRedis.redisAsync = this.async()
                    this@AbstractRedis.redisSync = this.sync()
                }
            }
            this@AbstractRedis.pubSub = it.connectPubSub().also { pubSub -> pubSub.addListener(Listener()) }
        }


        if (!logging) this.logger.level = Level.OFF

    }

    /**
     * Updates the value of the given key asynchronously.
     *
     * @param key The key to update.
     * @param data The data to update the key with.
     */
    fun updateKeyAsync(key: String, data: Any) {
        when (data is String || data is Number || data is Boolean) {
            true -> redisAsync.set(key, data.toString())
            false -> redisAsync.set(key, gson.toJson(data))
        }
    }

    /**
     * Updates the value of the given key synchronously.
     *
     * @param key The key to update.
     * @param data The data to update the key with.
     */
    fun updateKeySync(key: String, data: Any) {
        when (data is String || data is Number || data is Boolean) {
            true -> redisSync.set(key, data.toString())
            false -> redisSync.set(key, gson.toJson(data))
        }
    }

    /**
     * Gets the value of the given key synchronously.
     *
     * @param key The key to get the value of.
     *
     * @return the value of the given key, or null if the key does not exist.
     */
    fun getValueSync(key: String): String? = redisSync.get(key)

    /**
     * Gets the value of the given key asynchronously.
     *
     * @param key The key to get the value of.
     *
     * @return the value of the given key, or null if the key does not exist.
     */
    fun getValueAsync(key: String): RedisFuture<String?> = redisAsync.get(key)

    /**
     * Deletes the given key synchronously.
     * @param key The key to delete.
     * @see [redisSync]
     */
    fun deleteKeySync(vararg key: String) {
        redisSync.del(*key)
    }

    /**
     * Deletes the given key asynchronously.
     * @param key The key to delete.
     *
     * @see [redisAsync]
     */
    fun deleteKeyAsync(vararg key: String) {
        redisAsync.del(*key)
    }

    /**
     * Subscribes to the given channel. The given function will be called when a message is received.
     *
     * @param channel The channel to subscribe to.
     * @param function The function to call when a message is received.
     */
    fun subscribe(channel: String, function: (String, String) -> Unit) {
        pubSub.async().subscribe(channel)
        functions[channel] = function
    }

    /**
     * Unsubscribes the given channel.
     *
     * @param channel The channel to unsubscribe from.
     */
    fun unSubScribe(vararg channel: String) {
        pubSub.async().unsubscribe(*channel)
        logger.info("Unsubscribed from ${channel.joinToString(", ")}")
    }

    /**
     * This method publishes the given message to the given channel.
     *
     * @param channel The channel to publish to.
     * @param message The message to publish.
     */
    fun publish(channel: String, message: Any) {
        when (message is String || message is Number || message is Boolean) {
            true -> client.connectPubSub().async().publish(channel, message.toString())
            false -> client.connectPubSub().async().publish(channel, gson.toJson(message))
        }
        logger.info("Published to channel '$channel': '$message'")
    }

    fun getAsyncClient() = connection.async()
    fun getSyncClient() = connection.sync()

    private inner class Listener : RedisPubSubAdapter<String, String>() {

        override fun message(channel: String, message: String) =
            functions[channel]?.invoke(channel, message) ?: let {
                logger.log(
                    Level.WARNING,
                    "Received message on channel '$channel' but no function was registered for this channel."
                )
                unSubScribe(channel)
            }
    }
}