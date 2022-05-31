package de.mischmaschine.database.sql.network.mariadb

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

abstract class AbstractMariaDB(
    database: String,
) : AbstractNetworkSQL(
    database,
    DataBaseType.MARIADB,
)