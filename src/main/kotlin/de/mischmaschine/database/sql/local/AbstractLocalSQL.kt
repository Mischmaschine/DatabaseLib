package de.mischmaschine.database.sql.local

import de.mischmaschine.database.sql.AbstractSQL
import de.mischmaschine.database.sql.DataBaseType
import java.sql.Connection
import java.sql.DriverManager

abstract class AbstractLocalSQL(
    private val user: String?,
    private val password: String?,
    private val databasePath: String,
    private val dataBaseType: DataBaseType
) : AbstractSQL() {

    private var connection: Connection

    init {
        this.connection = createConnection()
    }


    /**
     * Creates a connection to the database.
     * @return the connection
     * @see DriverManager.getConnection
     * @see Connection
     */
    override fun getFreeDatabase(): Connection {
        return if (this.connection.isClosed) {
            createConnection().also { this.connection = it }
        } else {
            this.connection
        }
    }


    private fun createConnection(): Connection {
        return if (user == null && password == null) {
            DriverManager.getConnection("jdbc:${dataBaseType.name.lowercase()}:$databasePath")
        } else {
            DriverManager.getConnection("jdbc:${dataBaseType.name.lowercase()}:$databasePath", user, password)
        }
    }


    override fun closeConnection(connection: Connection) {
        this.connection.close()
    }
}