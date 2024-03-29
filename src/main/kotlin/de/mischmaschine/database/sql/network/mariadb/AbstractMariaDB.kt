package de.mischmaschine.database.sql.network.mariadb

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.network.AbstractNetworkSQL
import org.slf4j.Logger

/**
 * ## AbstractMariaDB
 * This class is a wrapper for the MariaDB JDBC driver.
 */
abstract class AbstractMariaDB(
    database: String,
) : AbstractNetworkSQL(
    Configuration.getHost(AbstractMariaDB::class),
    Configuration.getPort(AbstractMariaDB::class),
    Configuration.getUsername(AbstractMariaDB::class),
    Configuration.getPassword(AbstractMariaDB::class),
    database,
    DataBaseType.MARIADB,
), Database {
    override var logger: Logger? = null
}