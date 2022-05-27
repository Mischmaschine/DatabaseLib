package de.mischmaschine.database.sql.local.sqllite

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.local.AbstractLocalSQL

abstract class AbstractSQLite(
    databasePath: String
) : AbstractLocalSQL(
    null,
    null,
    databasePath,
    DataBaseType.SQLITE
)