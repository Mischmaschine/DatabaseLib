package de.mischmaschine.database.sql.postgresql

import de.mischmaschine.database.sql.AbstractSQL
import de.mischmaschine.database.DataBaseType

abstract class AbstractPostgreSQL(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
) : AbstractSQL(
    host,
    port,
    username,
    password,
    database,
    DataBaseType.POSTGRESQL
)