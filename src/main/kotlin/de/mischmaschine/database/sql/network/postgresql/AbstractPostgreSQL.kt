package de.mischmaschine.database.sql.network.postgresql

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

abstract class AbstractPostgreSQL(
    database: String,
) : AbstractNetworkSQL(
    database,
    DataBaseType.POSTGRESQL
)