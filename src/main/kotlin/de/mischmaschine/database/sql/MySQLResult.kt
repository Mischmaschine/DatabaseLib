package de.mischmaschine.database.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class MySQLResult(
    val connection: Connection,
    val prepareStatement: PreparedStatement,
    val resultSet: ResultSet
) : AutoCloseable {

    override fun close() {
        println("Close")
        try {
            this.connection.close()
            this.prepareStatement.close()
            this.resultSet.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

}