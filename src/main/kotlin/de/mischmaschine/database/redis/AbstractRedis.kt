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


abstract class AbstractRedis(database: Int) : Database {

    private val client: RedisClient
    private val connection: StatefulRedisConnection<String, String>
    private val gson = GsonBuilder().serializeNulls().create()
    private val redisSync: RedisCommands<String, String>
    private val redisAsync: RedisAsyncCommands<String, String>
    private val executors = mutableMapOf<String, (String, String) -> Unit>()
    private val pubSub: StatefulRedisPubSubConnection<String, String>

    init {

        val host = Configuration.getHost(AbstractRedis::class)
        val port = Configuration.getPort(AbstractRedis::class)
        val password = Configuration.getPassword(AbstractRedis::class)

        this.client = RedisClient.create(
            RedisURI.Builder.redis(
                host,
                port
            ).withPassword(password.toCharArray()).withDatabase(database).build()
        )
        this.connection = client.connect()
        this.redisAsync = connection.async()
        this.redisSync = connection.sync()
        this.pubSub = client.connectPubSub().also { it.addListener(Listener()) }
    }

    fun updateKeyAsync(key: String, data: Any) {
        redisAsync.set(key, gson.toJson(data))
    }

    fun updateKeySync(key: String, data: Any) {
        redisSync.set(key, gson.toJson(data))
    }

    fun getValueSync(key: String): String? = redisSync.get(key)

    fun getValueAsync(key: String): RedisFuture<String?> = redisAsync.get(key)

    fun deleteKeySync(vararg key: String) {
        redisSync.del(*key)
    }

    fun deleteKeyAsync(vararg key: String) {
        redisAsync.del(*key)
    }

    fun subscribe(channel: String, executor: (String, String) -> Unit) {
        pubSub.async().subscribe(channel)
        executors[channel] = executor
    }

    fun unSubScribe(vararg channel: String) {
        pubSub.async().unsubscribe(*channel)
        println("Unsubscribed from ${channel.joinToString(", ")}")
    }

    fun publish(channel: String, message: String) {
        client.connectPubSub().async().publish(channel, message)
        println("Published to $channel: $message")
    }

    fun getAsyncClient() = connection.async()
    fun getSyncClient() = connection.sync()

    inner class Listener : RedisPubSubAdapter<String, String>() {

        override fun message(channel: String, message: String) =
            executors[channel]?.invoke(channel, message) ?: let {
                println("There is no executor for channel $channel. Channel $channel will be ignored.")
                unSubScribe(channel)
            }
    }
}