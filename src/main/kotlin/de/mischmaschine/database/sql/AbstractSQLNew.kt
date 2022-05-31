package de.mischmaschine.database.sql

import com.google.gson.Gson
import de.mischmaschine.database.sql.annotations.Column
import de.mischmaschine.database.sql.annotations.PrimaryKey
import de.mischmaschine.database.sql.annotations.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.util.LinkedHashMap
import kotlin.coroutines.EmptyCoroutineContext

abstract class AbstractSQLNew {

    private val tableData = linkedMapOf<String, String>()
    private val tableValues = mutableListOf<Any>()
    private var primaryKey: String? = null

    abstract fun getFreeDatabase(): Connection
    abstract fun closeConnection(connection: Connection)

    fun initialize() {

        javaClass.declaredFields.filter { it.isAnnotationPresent(Column::class.java) }.forEach { field ->
            field.isAccessible = true
            println("Field-Name: ${field.name}")
            var fieldData = field.get(this) ?: return@forEach

            if (fieldData !is String && fieldData !is Boolean && fieldData !is Number) {
                fieldData = Gson().toJson(fieldData)
            }
            println("Field-Data: $fieldData")
            tableValues.add(fieldData.toString())

            tableData[field.name] = resolveSQLColumnType(field.type.typeName, field.getAnnotation(Column::class.java))
            if (primaryKey == null && field.isAnnotationPresent(PrimaryKey::class.java)) {
                primaryKey = field.name.lowercase()
            }
            println(" ")
        }
        println("| ---------------- |")
        println("| PrimaryKey: $primaryKey |")
        println("| ---------------- |")

    }
    // insertAsync(tableData, javaClass.getAnnotation(Table::class.java).table, tableValues)


    private fun resolveSQLColumnType(name: String, columnAnnotation: Column): String {
        if (columnAnnotation.maxLength == -1) {
            return "TEXT"
        }
        return when (name) {
            "int" -> {
                var maxLength = columnAnnotation.maxLength
                if (maxLength > 8) {
                    maxLength = 8
                }
                when (maxLength) {
                    1 -> "TINYINT(1)"
                    2 -> "SMALLINT(2)"
                    3 -> "MEDIUMINT(3)"
                    4 -> "INT(4)"
                    5, 6, 7, 8 -> "BIGINT($maxLength)"
                    else -> "BIGINT(8)"
                }
            }
            "boolean" -> {
                name.uppercase()
            }
            else -> "VARCHAR(${columnAnnotation.maxLength})"
        }
    }

    /**
     * This method is for inserting synchronized data in the database
     *
     * @param tableName Indicates on which table the SQL command is executed
     * @param tableValues a list from data, which will be inserted
     */
    private fun insertAsync(tableData: LinkedHashMap<String, String>, tableName: String, tableValues: List<Any>) {
        CoroutineScope(EmptyCoroutineContext).launch {
            val connection = getFreeDatabase()
            createTable(tableData, tableName, primaryKey, connection)
            val query = "INSERT INTO $tableName (${tableData.keys.joinToString()}) " +
                    "VALUES (${tableValues.joinToString("','", "'", "'")})"

            val prepareStatement = connection.prepareStatement(query)
            prepareStatement.executeUpdate()
            closeConnection(connection)
        }
    }

    private fun createTable(
        tableData: LinkedHashMap<String, String>,
        tableName: String,
        primaryKey: String?,
        connection: Connection
    ) {
        val tableCache = tableCache[this]
        if (tableCache == null || !tableCache.contains(tableName)) {
            tableCache?.add(tableName)
            val query = StringBuilder("CREATE TABLE IF NOT EXISTS $tableName (")
            for (key in tableData.keys) {
                query.append(key).append(" ").append(tableData[key]).append(", ")
            }
            primaryKey?.let {
                query.append("PRIMARY KEY (").append(it).append("));")
            }
            connection.prepareStatement(query.toString()).use { it.executeUpdate() }
        }

    }

    companion object {
        private val tableCache = mapOf<AbstractSQLNew, MutableList<String>>()
    }
}