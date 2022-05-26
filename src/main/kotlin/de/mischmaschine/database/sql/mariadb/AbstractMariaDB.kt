package de.mischmaschine.database.sql.mariadb

import de.mischmaschine.database.sql.AbstractSQL
import de.mischmaschine.database.DataBaseType

abstract class AbstractMariaDB(
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
    DataBaseType.MYSQL
)