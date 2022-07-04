package de.mischmaschine.database.database

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * @param host The host of the database.
 * @param port The port of the database.
 * @param username The username of the database.
 * @param password The password of the database.
 * @param kClazz The class of the database.
 *
 * This class is used to save the connection data details to a database.
 *
 * @see [JvmStatic]
 * @see [KClass]
 * @see [String]
 * @see [Int]
 */
class Configuration(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val kClazz: KClass<*>
) {

    /**
     * @param host The host of the database.
     * @param port The port of the database.
     * @param username The username of the database.
     * @param password The password of the database.
     * @param clazz The class of the database.
     *
     * This constructor the same as above, but you can use a java Class instead of a KClass.
     *
     * @see [Class]
     * @see [Class.kotlin]
     * @see [String]
     * @see [Int]
     */
    constructor(
        host: String,
        port: Int,
        username: String,
        password: String,
        clazz: Class<*>
    ) : this(host, port, username, password, clazz.kotlin)

    init {
        // Checks if the class is abstract.
        if (!this.kClazz.isAbstract) {
            throw IllegalArgumentException("Class must be abstract")
        }

        // Check if the class is a subclass of the Database interface
        if (!this.kClazz.isSubclassOf(Database::class)) {
            throw IllegalArgumentException("Class must be subclass of Configuration")
        }

        setConnectionCredentials()
    }

    /**
     * This method is used to get the simple name of the class.
     * @return The simple name of the class.
     */
    private fun resolveConfigurationSimpleName(): String {
        return this.kClazz.simpleName ?: throw IllegalArgumentException("Class must have a simple name")
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
    private fun setConnectionCredentials() {
        connectionMap[resolveConfigurationSimpleName()] = this
    }

    companion object {
        val connectionMap = mutableMapOf<String, Configuration>()

        /**
         * This method is used to get the host of the database.
         * @param kClass The class of the database.
         * @return The host of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getHost(kClass: KClass<*>): String {
            return (connectionMap[kClass.simpleName]?.host
                ?: throw IllegalArgumentException("Class is not configured"))
        }

        /**
         * This method is used to get the port of the database.
         * @param kClass The class of the database.
         * @return The port of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getPort(kClass: KClass<*>): Int {
            return (connectionMap[kClass.simpleName]?.port
                ?: throw IllegalArgumentException("Class is not configured"))
        }

        /**
         * This method is used to get the username of the database.
         * @param kClass The class of the database.
         * @return The username of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getUsername(kClass: KClass<*>): String {
            return (connectionMap[kClass.simpleName]?.username
                ?: throw IllegalArgumentException("Class is not configured"))
        }

        /**
         * This method is used to get the password of the database.
         * @param kClass The class of the database.
         * @return The password of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getPassword(kClass: KClass<*>): String {
            return (connectionMap[kClass.simpleName]?.password
                ?: throw IllegalArgumentException("Class is not configured"))
        }
    }

}