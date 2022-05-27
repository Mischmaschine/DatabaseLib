package de.mischmaschine.database.sql.network.mysql

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

abstract class AbstractMySQL(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
) : AbstractNetworkSQL(
    host,
    port,
    username,
    password,
    database,
    DataBaseType.MYSQL,
)