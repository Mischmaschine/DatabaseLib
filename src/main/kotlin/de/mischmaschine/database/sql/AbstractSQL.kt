package de.mischmaschine.database.sql

import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.SQLException
import kotlin.coroutines.EmptyCoroutineContext

abstract class AbstractSQL {

    private var tableData = mutableListOf<String>()

    fun updateSync(
        tableName: String,
        columnName: String,
        key: Any,
        targetKey: String,
        targetValue: String
    ) {
        val query = "UPDATE $tableName SET $columnName='$key' WHERE $targetKey='$targetValue'"
        println(query)
        updateQuery(query)
    }

    private fun updateQuery(query: String) {
        val connection = getFreeDatabase()
        val prepareStatement = connection.prepareStatement(query)
        prepareStatement.executeUpdate()
    }

    fun updateAsync(
        tableName: String,
        columnName: String,
        key: Any,
        targetKey: String,
        targetValue: String
    ) {
        CoroutineScope(EmptyCoroutineContext).launch {
            updateSync(tableName, columnName, key, targetKey, targetValue)
        }
    }


    fun insertSync(tableName: String, tableValues: List<Any>) {
        insertQuery(
            "INSERT INTO $tableName (${tableData.joinToString()}) VALUES (${
                tableValues.joinToString("','", "'", "'")
            })"
        )
    }

    fun insertAsync(tableName: String, tableData: List<Any>) {
        CoroutineScope(EmptyCoroutineContext).launch {
            insertSync(tableName, tableData)
        }
    }

    private fun insertQuery(query: String) {
        val connection = getFreeDatabase()
        val prepareStatement = connection.prepareStatement(query)
        prepareStatement.executeUpdate()
        closeConnection(connection)
    }

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

    fun getResultAsync(
        tableName: String,
        key: String,
        value: String,
        additionalQuery: String = ""
    ): Deferred<MySQLResult?> {
        return CoroutineScope(EmptyCoroutineContext).async {
            getResultSync(tableName, key, value, additionalQuery)
        }
    }

    fun deleteSync(tableName: String, targetKey: String, targetValue: Any) =
        deleteQuery("DELETE FROM $tableName WHERE $targetKey='$targetValue'")


    fun deleteAsync(tableName: String, targetKey: String, targetValue: Any) =
        CoroutineScope(EmptyCoroutineContext).launch {
            deleteSync(tableName, targetKey, targetValue)
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