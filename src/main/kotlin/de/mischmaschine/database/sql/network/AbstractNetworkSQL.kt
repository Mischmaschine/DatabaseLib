package de.mischmaschine.database.sql.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.mischmaschine.database.sql.AbstractSQL
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.configuration.SQLConfiguration
import java.sql.Connection

abstract class AbstractNetworkSQL(
    database: String,
    dataBaseType: DataBaseType
) : AbstractSQL() {

    private var dataSource: HikariDataSource

    init {
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl =
                "jdbc:${dataBaseType.name.lowercase()}://${SQLConfiguration.getHost()}:${SQLConfiguration.getPort()}/$database"
            this.username = SQLConfiguration.getUsername()
            this.password = SQLConfiguration.getPassword()
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