package de.mischmaschine.database.sql.network.mariadb

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

abstract class AbstractMariaDB(
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