package de.mischmaschine.database.sql.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.AbstractSQL
import java.sql.Connection

abstract class AbstractNetworkSQL(
    host: String,
    port: Int,
    tempUsername: String,
    tempPassword: String,
    database: String,
    dataBaseType: DataBaseType
) : AbstractSQL() {

    private val dataSource: HikariDataSource

    init {
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = "jdbc:${dataBaseType.name.lowercase()}://$host:$port/$database"
            this.username = tempUsername
            this.password = tempPassword
        }
        dataSource = HikariDataSource(hikariConfig)
    }

    override fun getFreeDatabase(): Connection {
        return this.dataSource.connection
    }
}