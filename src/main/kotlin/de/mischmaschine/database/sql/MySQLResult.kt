package de.mischmaschine.database.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * @param connection The connection to the database.
 * @param prepareStatement The prepare statement to use.
 * @param resultSet The result set to use.
 *
 * @see Connection
 * @see PreparedStatement
 * @see ResultSet
 * @see SQLException
 */
class MySQLResult(
    val connection: Connection,
    val prepareStatement: PreparedStatement,
    val resultSet: ResultSet
) : AutoCloseable {

    override fun close() {
        try {
            this.connection.close()
            this.prepareStatement.close()
            this.resultSet.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

}