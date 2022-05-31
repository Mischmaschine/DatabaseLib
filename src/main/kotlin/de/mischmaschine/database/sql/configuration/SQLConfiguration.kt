package de.mischmaschine.database.sql.configuration

class SQLConfiguration(
    host: String,
    port: Int,
    username: String,
    password: String,
) {

    init {
        sqlDataMap["host"] = host
        sqlDataMap["port"] = port.toString()
        sqlDataMap["username"] = username
        sqlDataMap["password"] = password
    }

    companion object {

        val sqlDataMap = mutableMapOf<String, String>()

        fun getHost(): String? {
            return sqlDataMap["host"]
        }

        fun getPort(): String? {
            return sqlDataMap["port"]
        }

        fun getUsername(): String? {
            return sqlDataMap["username"]
        }

        fun getPassword(): String? {
            return sqlDataMap["password"]
        }
    }
}