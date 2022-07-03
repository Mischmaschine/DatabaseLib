package de.mischmaschine.database.database

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * This class is used to save the connection data details to a database.
 * It also is usable in java.
 * @see [JvmStatic]
 * @see [KClass]
 * @see [KClass.isSubclassOf]
 * @see [KClass.simpleName]
 */
class Configuration(
    host: String,
    port: Int,
    username: String,
    password: String,
    private val clazz: KClass<*>
) {

    init {
        if (!this.clazz.isAbstract) {
            throw IllegalArgumentException("Class must be abstract")
        }

        if (!this.clazz.isSubclassOf(Database::class)) {
            throw IllegalArgumentException("Class must be subclass of Configuration")
        }

        setConnectionCredentials(host, port, username, password)
    }

    /**
     * This method is used to get the simple name of the class.
     * @return The simple name of the class.
     */
    private fun resolveConfigurationSimpleName(): String {
        return this.clazz.simpleName ?: throw IllegalArgumentException("Class must have a simple name")
    }

    /**
     * This method is used to set the connection credentials.
     * @param host The host of the database.
     * @param port The port of the database.
     * @param username The username of the database.
     * @param password The password of the database.
     *
     * @see [resolveConfigurationSimpleName]
     * @see [Database]
     * @see [connectionMap]
     * @see [mutableMapOf]
     */
    private fun setConnectionCredentials(host: String, port: Int, username: String, password: String) {
        val classSimpleName = resolveConfigurationSimpleName()
        val connectionMapForClass = connectionMap[classSimpleName] ?: mutableMapOf()
        connectionMapForClass["host"] = host
        connectionMapForClass["port"] = port.toString()
        connectionMapForClass["username"] = username
        connectionMapForClass["password"] = password
        connectionMap[classSimpleName] = connectionMapForClass
    }

    companion object {
        val connectionMap = mutableMapOf<String, MutableMap<String, String>>()

        /**
         * This method is used to get the host of the database.
         * @param kClass The class of the database.
         * @return The host of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getHost(kClass: KClass<*>): String {
            return connectionMap[kClass.simpleName]?.get("host")
                ?: throw IllegalArgumentException("Class is not configured 1")
        }

        /**
         * This method is used to get the port of the database.
         * @param kClass The class of the database.
         * @return The port of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getPort(kClass: KClass<*>): Int {
            return connectionMap[kClass.simpleName]?.get("port")?.toInt()
                ?: throw IllegalArgumentException("Class is not configured 2")
        }

        /**
         * This method is used to get the username of the database.
         * @param kClass The class of the database.
         * @return The username of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getUsername(kClass: KClass<*>): String {
            return connectionMap[kClass.simpleName]?.get("username")
                ?: throw IllegalArgumentException("Class is not configured 3")
        }

        /**
         * This method is used to get the password of the database.
         * @param kClass The class of the database.
         * @return The password of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getPassword(kClass: KClass<*>): String {
            return connectionMap[kClass.simpleName]?.get("password")
                ?: throw IllegalArgumentException("Class is not configured 4")
        }
    }

}