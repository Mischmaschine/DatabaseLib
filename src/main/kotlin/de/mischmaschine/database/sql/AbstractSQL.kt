package de.mischmaschine.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.mischmaschine.database.DataBaseType
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.SQLException
import kotlin.coroutines.EmptyCoroutineContext

abstract class AbstractSQL(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val database: String,
    val databaseType: DataBaseType
) {

    var dataSource: HikariDataSource
    private var tableData = mutableListOf<String>()

    init {
        val hikariConfig = HikariConfig()
        val jdbcUrl = "jdbc:${databaseType.name.lowercase()}://$host:$port/$database"
        hikariConfig.jdbcUrl = jdbcUrl
        hikariConfig.username = this.username
        hikariConfig.password = this.password
        dataSource = HikariDataSource(hikariConfig)
    }

    fun updateSync(
        tableName: String,
        columnName: String,
        key: String,
        targetKey: String,
        targetValue: String,
    ) {
        val query = "UPDATE $tableName SET $columnName='$key' WHERE $targetKey='$targetValue'"
        println(query)
        updateQuery(query)
    }

    private fun updateQuery(query: String) {
        getFreeDatabase().use {
            val prepareStatement = it.prepareStatement(query)
            prepareStatement.executeUpdate()
        }
    }

    fun updateAsync(
        tableName: String,
        key: String,
        value: String,
        targetKey: String,
        targetValue: String,
    ) {
        CoroutineScope(EmptyCoroutineContext).launch {
            updateSync(tableName, key, value, targetKey, targetValue)
        }
    }


    fun insertSync(tableName: String, tableValues: List<String>) {
        insertQuery(
            "INSERT INTO $tableName (${tableData.joinToString()}) VALUES (${
                tableValues.joinToString("','", "'", "'")
            })"
        )
    }

    fun insertAsync(tableName: String, tableData: List<String>) {
        CoroutineScope(EmptyCoroutineContext).launch {
            insertSync(tableName, tableData)
        }
    }

    private fun insertQuery(query: String) {
        getFreeDatabase().use {
            val prepareStatement = it.prepareStatement(query)
            prepareStatement.executeUpdate()
        }
    }

    fun getResultSync(tableName: String, columnName: String, key: String, additionalQuery: String = ""): MySQLResult? {
        try {
            getFreeDatabase().use {
                val queryString = "SELECT * FROM $tableName WHERE $columnName='$key' $additionalQuery"
                val prepareStatement = it.prepareStatement(queryString)
                return MySQLResult(it, prepareStatement, prepareStatement.executeQuery())
            }
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

    fun deleteSync(tableName: String, targetKey: String, targetValue: String) =
        deleteQuery("DELETE FROM $tableName WHERE $targetKey='$targetValue'")


    fun deleteAsync(tableName: String, targetKey: String, targetValue: String) =
        CoroutineScope(EmptyCoroutineContext).launch {
            deleteSync(tableName, targetKey, targetValue)
        }

    private fun deleteQuery(query: String) {
        getFreeDatabase().use {
            val prepareStatement = it.prepareStatement(query)
            prepareStatement.executeUpdate()
        }
    }

    fun getFreeDatabase(): Connection = dataSource.connection

    fun createTable(tableData: LinkedHashMap<String, String>, tableName: String, primaryKey: String) {
        val query = StringBuilder("CREATE TABLE IF NOT EXISTS $tableName (")
        require(tableData.keys.contains(primaryKey)) { "PrimaryKey ist nicht in der Tabelle enthalten!" }
        for (key in tableData.keys) {
            this.tableData.add(key)
            query.append(key).append(" ").append(tableData[key]).append(", ")
        }
        query.append("PRIMARY KEY (").append(primaryKey).append("));")
        getFreeDatabase().use {
            it.prepareStatement(query.toString()).use { ps -> ps.executeUpdate() }
        }
    }
}