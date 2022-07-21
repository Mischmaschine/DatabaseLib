package de.mischmaschine.database.redis

import com.google.gson.GsonBuilder
import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import org.redisson.Redisson
import org.redisson.api.RFuture
import org.redisson.api.RedissonClient
import org.redisson.codec.JsonJacksonCodec
import org.redisson.config.Config

abstract class AbstractRedis(private val database: Int) : Database {

    private val client: RedissonClient
    private val gson = GsonBuilder().serializeNulls().create()

    init {
        val config = Config().apply {
            codec = JsonJacksonCodec()
            useSingleServer().apply {
                address =
                    "redis://" + Configuration.getHost(AbstractRedis::class) + ":" + Configuration.getPort(AbstractRedis::class)
                idleConnectionTimeout = 10000
                timeout = 3000
                retryInterval = 1000
                dnsMonitoringInterval = 5000
                subscriptionsPerConnection = 2
                subscriptionConnectionPoolSize = 25
                subscriptionConnectionMinimumIdleSize = 1
                password = Configuration.getPassword(AbstractRedis::class)
                database = this@AbstractRedis.database
            }
        }
        this.client = Redisson.create(config);
    }

    fun updateKeyAsync(key: String, data: Any) {
        val rBucket = getClient().getBucket<String>(key)
        rBucket.setAsync(gson.toJson(data))
    }

    fun updateKeySync(key: String, data: Any) {
        val rBucket = getClient().getBucket<String>(key)
        rBucket.set(gson.toJson(data))
    }

    fun getKeySync(key: String): String {
        val rBucket = getClient().getBucket<String>(key)
        return rBucket.get()
    }

    fun getKeyAsync(key: String): RFuture<String> {
        val rBucket = getClient().getBucket<String>(key)
        return rBucket.async
    }

    fun deleteKeySync(key: String) {
        getClient().getBucket<String>(key).delete()
    }

    fun deleteKeyAsync(key: String) {
        getClient().getBucket<String>(key).deleteAsync()
    }

    fun existsSync(key: String): Boolean {
        return getClient().getBucket<String>(key).isExists
    }

    fun existsAsync(key: String): RFuture<Boolean> {
        return getClient().getBucket<String>(key).isExistsAsync
    }

    fun getBucket(key: String) = getClient().getBucket<String>(key)

    fun getClient(): RedissonClient {
        return client
    }
}