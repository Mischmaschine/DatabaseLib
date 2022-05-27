package de.mischmaschine.database.sql.local.sqllite

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.local.AbstractLocalSQL

class SQLLite(
    databasePath: String
) : AbstractLocalSQL(
    null,
    null,
    databasePath,
    DataBaseType.SQLLITE
)