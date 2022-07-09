package de.mischmaschine.database.sql.network.mysql

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL

/**
 * ## AbstractMySQL
 * This class is a wrapper for the MySQL JDBC driver.
 */
abstract class AbstractMySQL(
    database: String,
) : AbstractNetworkSQL(
    Configuration.getHost(AbstractMySQL::class),
    Configuration.getPort(AbstractMySQL::class),
    Configuration.getUsername(AbstractMySQL::class),
    Configuration.getPassword(AbstractMySQL::class),
    database,
    DataBaseType.MYSQL
), Database