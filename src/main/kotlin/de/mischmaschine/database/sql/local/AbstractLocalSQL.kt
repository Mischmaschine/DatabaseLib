package de.mischmaschine.database.sql.local

import de.mischmaschine.database.sql.AbstractSQL
import de.mischmaschine.database.sql.DataBaseType
import java.sql.Connection
import java.sql.DriverManager

abstract class AbstractLocalSQL(
    user: String?,
    password: String?,
    databasePath: String,
    dataBaseType: DataBaseType
) : AbstractSQL() {

    private val connection: Connection

    init {
        this.connection = if (user == null && password == null) {
            DriverManager.getConnection("jdbc:${dataBaseType.name.lowercase()}:$databasePath")
        } else {
            DriverManager.getConnection("jdbc:${dataBaseType.name.lowercase()}:$databasePath", user, password)
        }
    }

    override fun getFreeDatabase(): Connection {
        return this.connection
    }

    override fun closeConnection(connection: Connection) {

    }
}