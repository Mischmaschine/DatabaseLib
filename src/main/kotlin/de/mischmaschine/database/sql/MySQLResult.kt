package de.mischmaschine.database.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * ## MySQLResult
 * @property connection The connection to the database.
 * @property prepareStatement The prepare statement to use.
 * @property resultSet The result set to use.
 *
 * @see Connection
 * @see PreparedStatement
 * @see ResultSet
 * @see SQLException
 */
class MySQLResult(
    private val connection: Connection,
    private val prepareStatement: PreparedStatement,
    private val resultSet: ResultSet
) : AutoCloseable {

    /**
     * Closes everything from to connection to the database.
     */
    override fun close() {
        try {
            this.connection.close()
            this.prepareStatement.close()
            this.resultSet.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * @return The result set of the query.
     */
    fun getResultSet(): ResultSet {
        return this.resultSet
    }

    /**
     * @return The prepare statement of the query.
     */
    fun getPrepareStatement(): PreparedStatement {
        return this.prepareStatement
    }

    /**
     * @return The connection to the database.
     */
    fun getConnection(): Connection {
        return this.connection
    }
}