package de.mischmaschine.database.mongodb.configuration

class MongoConfiguration(
    host: String,
    port: Int,
    username: String,
    password: String,
) {

    init {
        mongoDataMap["host"] = host
        mongoDataMap["port"] = port.toString()
        mongoDataMap["username"] = username
        mongoDataMap["password"] = password
    }

    companion object {

        val mongoDataMap = mutableMapOf<String, String>()

        fun getHost(): String? {
            return mongoDataMap["host"]
        }

        fun getPort(): String? {
            return mongoDataMap["port"]
        }

        fun getUsername(): String? {
            return mongoDataMap["username"]
        }

        fun getPassword(): String? {
            return mongoDataMap["password"]
        }
    }
}