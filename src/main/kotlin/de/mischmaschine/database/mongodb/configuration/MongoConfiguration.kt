package de.mischmaschine.database.mongodb.configuration

@Deprecated("Use de.mischmaschine.database.database.configuration.Configuration instead")
class MongoConfiguration(
    host: String,
    port: Int,
    username: String,
    password: String,
) {

    init {
        throw UnsupportedOperationException("Use de.mischmaschine.database.database.configuration.Configuration instead")
        mongoDataMap["host"] = host
        mongoDataMap["port"] = port.toString()
        mongoDataMap["username"] = username
        mongoDataMap["password"] = password
    }

    companion object {

        private val mongoDataMap = mutableMapOf<String, String>()

        @JvmStatic
        fun getHost(): String? {
            return mongoDataMap["host"]
        }

        @JvmStatic
        fun getPort(): String? {
            return mongoDataMap["port"]
        }

        @JvmStatic
        fun getUsername(): String? {
            return mongoDataMap["username"]
        }

        @JvmStatic
        fun getPassword(): String? {
            return mongoDataMap["password"]
        }
    }
}