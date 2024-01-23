package de.mischmaschine.database.sql.local.sqlite

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.local.AbstractLocalSQL
import org.slf4j.Logger

/**
 * ## AbstractSQLite
 * This class is a wrapper for the SQLite JDBC driver.
 */
abstract class AbstractSQLite(
    databasePath: String
) : AbstractLocalSQL(
    Configuration.getUsername(AbstractSQLite::class),
    Configuration.getPassword(AbstractSQLite::class),
    databasePath,
    DataBaseType.SQLITE
), Database {
    override var logger: Logger? = null
}