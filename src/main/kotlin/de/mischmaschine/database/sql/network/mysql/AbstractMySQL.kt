package de.mischmaschine.database.sql.network.mysql

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

abstract class AbstractMySQL(
    database: String,
) : AbstractNetworkSQL(
    database,
    DataBaseType.MYSQL
)