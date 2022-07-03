package de.mischmaschine.database.sql.configuration

@Deprecated(
    "Use de.mischmaschine.database.database.configuration.Configuration instead",
    level = DeprecationLevel.ERROR
)
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

        private val sqlDataMap = mutableMapOf<String, String>()

        @JvmStatic
        fun getHost(): String? {
            return sqlDataMap["host"]
        }

        @JvmStatic
        fun getPort(): String? {
            return sqlDataMap["port"]
        }

        @JvmStatic
        fun getUsername(): String? {
            return sqlDataMap["username"]
        }

        @JvmStatic
        fun getPassword(): String? {
            return sqlDataMap["password"]
        }
    }
}