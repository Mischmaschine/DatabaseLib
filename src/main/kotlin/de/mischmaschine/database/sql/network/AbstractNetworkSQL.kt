package de.mischmaschine.database.sql.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.mischmaschine.database.sql.AbstractSQL
import de.mischmaschine.database.sql.DataBaseType
import java.sql.Connection

abstract class AbstractNetworkSQL(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
    dataBaseType: DataBaseType
) : AbstractSQL() {

    private var dataSource: HikariDataSource

    init {
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl =
                "jdbc:${dataBaseType.name.lowercase()}://${host}:${port}/$database"
            this.username = username
            this.password = password
        }
        this.dataSource = HikariDataSource(hikariConfig)
    }

    override fun getFreeDatabase(): Connection {
        return this.dataSource.connection
    }

    override fun closeConnection(connection: Connection) {
        connection.close()
    }
}