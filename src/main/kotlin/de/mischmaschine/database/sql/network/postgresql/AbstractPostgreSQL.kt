package de.mischmaschine.database.sql.network.postgresql

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

/**
 * ## PostgreSQL
 *
 * This class is a wrapper for the PostgreSQL JDBC driver.
 */
abstract class AbstractPostgreSQL(
    database: String,
) : AbstractNetworkSQL(
    Configuration.getHost(AbstractPostgreSQL::class),
    Configuration.getPort(AbstractPostgreSQL::class),
    Configuration.getUsername(AbstractPostgreSQL::class),
    Configuration.getPassword(AbstractPostgreSQL::class),
    database,
    DataBaseType.POSTGRESQL
), Database