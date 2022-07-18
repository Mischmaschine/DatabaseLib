package de.mischmaschine.database.sql

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.sql.Connection
import java.util.concurrent.CompletableFuture

abstract class AbstractSQL {

    private var tableData = mutableListOf<String>()
    private var uniqueId = "unique_id"

    /**
     * This method is for updating synchronized  row in the database.
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param key       Indicates the key of the row which is updated
     * @param columnName Specifies to which column name a value should be changed.
     * @param updatedValue Specifies the new value which should be set to the column.
     */
    fun updateSync(tableName: String, key: String, columnName: String, updatedValue: Any) {
        val serializedValue = if (updatedValue !is String && updatedValue !is Boolean && updatedValue !is Number) {
            gson.toJson(updatedValue)
        } else {
            updatedValue
        }
        if (!tableData.contains(columnName)) throw IllegalArgumentException("Column $columnName does not exist in table $tableName")
        val query = "UPDATE $tableName SET $columnName='$serializedValue' WHERE $uniqueId='$key'"
        updateQuery(query)
    }

    /**
     * This method is for updating synchronized  row in the database.
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param key       Indicates the key of the row which is updated
     * @param columnName Specifies to which column name a value should be changed.
     * @param updatedValue Specifies the new value which should be set to the column.
     */
    fun updateAsync(tableName: String, key: String, columnName: String, updatedValue: Any) {
        CompletableFuture.runAsync {
            updateSync(tableName, key, columnName, updatedValue)
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
        val serializedList = mutableListOf<Any>()
        tableValues.forEach {
            val toAdd = if (it !is String && it !is Boolean && it !is Number) {
                gson.toJson(it)
            } else {
                it
            }
            serializedList.add(toAdd)
        }
        val query = "INSERT INTO $tableName (${tableData.joinToString()}) VALUES (${
            serializedList.joinToString("','", "'", "'")
        })"
        insertQuery(query)
    }

    /**
     * This method is for inserting asynchronously data in the database

     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param tableValues a list from data, which will be inserted
     */
    fun insertAsync(tableName: String, tableValues: List<Any>) {
        CompletableFuture.runAsync {
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
     * @param key where the database should get a result from
     *
     * @return the MySQLResult or null
     */
    fun getResultSync(tableName: String, key: String, additionalQuery: String = ""): MySQLResult {
        val queryString = "SELECT * FROM $tableName WHERE $uniqueId='$key' $additionalQuery"
        val connection = getFreeDatabase()
        val prepareStatement = connection.prepareStatement(queryString)
        return MySQLResult(connection, prepareStatement, prepareStatement.executeQuery())
    }

    /**
     *
     * This method is for getting asynchronously data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param key where the database should get a result from
     * @return the MySQLResult
     */
    fun getResultAsync(
        tableName: String,
        key: String,
        additionalQuery: String = ""
    ): CompletableFuture<MySQLResult?> {
        return CompletableFuture.supplyAsync {
            getResultSync(tableName, key, additionalQuery)
        }
    }

    /**
     * This method is for deleting synchronized data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param key which row should be deleted specified by the key
     */
    fun deleteSync(tableName: String, key: String) =
        deleteQuery("DELETE FROM $tableName WHERE $uniqueId='$key'")

    /**
     * This method is for deleting asynchronously data from the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param key which row should be deleted specified by the key
     */
    fun deleteAsync(tableName: String, key: String) {
        CompletableFuture.supplyAsync {
            deleteSync(tableName, key)
        }
    }

    private fun deleteQuery(query: String) {
        val connection = getFreeDatabase()

        val prepareStatement = connection.prepareStatement(query)
        prepareStatement.executeUpdate()
        closeConnection(connection)
    }

    /**
     * @return the connection to the database
     */
    abstract fun getFreeDatabase(): Connection

    /**
     * @return closes the connection
     */
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

    companion object {
        private val gson: Gson = GsonBuilder().serializeNulls().create()
    }
}