package de.mischmaschine.database.sql

import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

abstract class AbstractSQL {

    private var tableData = mutableListOf<String>()

    /**
     * This method is for updating synchronized  row in the database.
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param columnName Specifies to which column name a value should be changed.
     * @param value Data to be updated
     * @param targetColumnName Column name where for which row the data should be updated
     * @param targetValue Value to find the row, where the data should be updated
     */
    fun updateSync(
        tableName: String,
        columnName: String,
        value: Any,
        targetColumnName: String,
        targetValue: String
    ) {
        val query = "UPDATE $tableName SET $columnName='$value' WHERE $targetColumnName='$targetValue'"
        println(query)
        updateQuery(query)
    }


    /**
     * This method is for updating asynchronously a row in the database.
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param columnName Specifies to which column name a value should be changed.
     * @param value Data to be updated
     * @param targetColumnName Column name where for which row the data should be updated
     * @param targetValue Value to find the row, where the data should be updated
     */
    fun updateAsync(
        tableName: String,
        columnName: String,
        value: Any,
        targetColumnName: String,
        targetValue: String
    ) {
        CoroutineScope(EmptyCoroutineContext).launch {
            updateSync(tableName, columnName, value, targetColumnName, targetValue)
        }
    }

    private fun updateQuery(query: String) {
        val connection = getFreeDatabase()
        val prepareStatement = connection.prepareStatement(query)
        prepareStatement.executeUpdate()
    }

    /**
     * This method is for inserting synchronized data in the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param tableValues a list from data, which will be inserted
     */
    fun insertSync(tableName: String, tableValues: List<Any>) {
        val query = "INSERT INTO $tableName (${tableData.joinToString()}) VALUES (${
            tableValues.joinToString("','", "'", "'")
        })"
        println(query)
        insertQuery(query)
    }

    /**
     * This method is for inserting asynchronously data in the database

     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param tableValues a list from data, which will be inserted
     */
    fun insertAsync(tableName: String, tableValues: List<Any>) {
        CoroutineScope(EmptyCoroutineContext).launch {
            insertSync(tableName, tableValues)
        }
    }

    private fun insertQuery(query: String) {
        val connection = getFreeDatabase()
        val prepareStatement = connection.prepareStatement(query)
        prepareStatement.executeUpdate()
        closeConnection(connection)
    }

    /**
     *
     * This method is for getting synchronized data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param columnName Specifies to which column name a value should be changed.
     * @param key where the database should get a result from
     * @return the MySQLResult or null
     */
    fun getResultSync(tableName: String, columnName: String, key: String, additionalQuery: String = ""): MySQLResult? {
        try {
            val queryString = "SELECT * FROM $tableName WHERE $columnName='$key' $additionalQuery"
            val connection = getFreeDatabase()
            val prepareStatement = connection.prepareStatement(queryString)
            return MySQLResult(connection, prepareStatement, prepareStatement.executeQuery())

        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     *
     * This method is for getting asynchronously data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param columnName Specifies to which column name a value should be changed.
     * @param key where the database should get a result from
     * @return the MySQLResult or null
     */
    fun getResultAsync(
        tableName: String,
        columnName: String,
        key: String,
        additionalQuery: String = ""
    ): Deferred<MySQLResult?> {
        return CoroutineScope(EmptyCoroutineContext).async {
            getResultSync(tableName, columnName, key, additionalQuery)
        }
    }

    /**
     * This method is for deleting synchronized data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param columnName in which column name a value should be deleted
     * @param value which row should be deleted specified by the value
     */
    fun deleteSync(tableName: String, columnName: String, value: Any) =
        deleteQuery("DELETE FROM $tableName WHERE $columnName='$value'")

    /**
     * This method is for deleting asynchronously data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param columnName in which column name a value should be deleted
     * @param value which row should be deleted specified by the value
     */
    fun deleteAsync(tableName: String, columnName: String, value: Any) =
        CoroutineScope(EmptyCoroutineContext).launch {
            deleteSync(tableName, columnName, value)
        }

    private fun deleteQuery(query: String) {
        val connection = getFreeDatabase()

        val prepareStatement = connection.prepareStatement(query)
        prepareStatement.executeUpdate()
        closeConnection(connection)
    }

    abstract fun getFreeDatabase(): Connection
    abstract fun closeConnection(connection: Connection)

    fun createTable(tableData: LinkedHashMap<String, String>, tableName: String, primaryKey: String) {
        val query = StringBuilder("CREATE TABLE IF NOT EXISTS $tableName (")
        require(tableData.keys.contains(primaryKey)) { "PrimaryKey ist nicht in der Tabelle enthalten!" }
        for (key in tableData.keys) {
            this.tableData.add(key)
            query.append(key).append(" ").append(tableData[key]).append(", ")
        }
        query.append("PRIMARY KEY (").append(primaryKey).append("));")
        val connection = getFreeDatabase()
        connection.prepareStatement(query.toString()).use { it.executeUpdate() }

    }
}