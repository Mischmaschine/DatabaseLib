package de.mischmaschine.database.sql.local.sqlite

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.local.AbstractLocalSQL

abstract class AbstractSQLite(
    databasePath: String
) : AbstractLocalSQL(
    Configuration.getUsername(AbstractSQLite::class),
    Configuration.getPassword(AbstractSQLite::class),
    databasePath,
    DataBaseType.SQLITE
), Database