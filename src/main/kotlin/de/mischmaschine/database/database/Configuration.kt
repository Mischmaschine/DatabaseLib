package de.mischmaschine.database.database

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * This class is used to save the connection data details to a database.
 *
 * @property host The host of the database.
 * @property port The port of the database.
 * @property username The username of the database.
 * @property password The password of the database.
 * @property kClazz The class of the database.
 *
 * @see [JvmStatic]
 * @see [KClass]
 * @see [String]
 * @see [Int]
 */
class Configuration<T : Database>(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val kClazz: KClass<T>,
) {

    /**
     * @property host The host of the database.
     * @property port The port of the database.
     * @property username The username of the database.
     * @property password The password of the database.
     * @property clazz The class of the database.
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
        clazz: Class<T>,
    ) : this(host, port, username, password, clazz.kotlin)

    init {
        setConnectionCredentials()
    }

    /**
     * This method is used to get the simple name of the class.
     * @return The simple name of the class.
     */
    private fun resolveConfigurationSimpleName(): String {
        val simpleName = this.kClazz.simpleName
        require(simpleName != null) { "Class must have a simple name" }
        return simpleName
    }

    /**
     * This method is used to set the connection credentials.
     *
     * @see [resolveConfigurationSimpleName]
     * @see [connectionMap]
     * @see [mutableMapOf]
     */
    private fun setConnectionCredentials() {
        connectionMap[resolveConfigurationSimpleName()] = this
    }

    companion object {
        val connectionMap = mutableMapOf<String, Configuration<*>>()

        /**
         * This method is used to get the host of the database.
         *
         * @param kClass The class of the database.
         * @return The host of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getHost(kClass: KClass<*>): String {
            val result = connectionMap[kClass.simpleName]
            require(result != null) { "Class is not configured" }
            return result.host
        }

        /**
         * This method is used to get the port of the database.
         *
         * @param kClass The class of the database.
         * @return The port of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getPort(kClass: KClass<*>): Int {
            val result = connectionMap[kClass.simpleName]
            require(result != null) { "Class is not configured" }
            return result.port
        }

        /**
         * This method is used to get the username of the database.
         *
         * @param kClass The class of the database.
         * @return The username of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getUsername(kClass: KClass<*>): String {
            val result = connectionMap[kClass.simpleName]
            require(result != null) { "Class is not configured" }
            return result.username
        }

        /**
         * This method is used to get the password of the database.
         *
         * @param kClass The class of the database.
         * @return The password of the database.
         * @throws IllegalArgumentException If the class is not a subclass of [Database].
         */
        @JvmStatic
        fun getPassword(kClass: KClass<*>): String {
            val result = connectionMap[kClass.simpleName]
            require(result != null) { "Class is not configured" }
            return result.password
        }
    }

}
