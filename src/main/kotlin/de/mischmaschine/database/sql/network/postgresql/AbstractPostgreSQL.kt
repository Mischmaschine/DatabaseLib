package de.mischmaschine.database.sql.network.postgresql

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

abstract class AbstractPostgreSQL(
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
    DataBaseType.POSTGRESQL
)