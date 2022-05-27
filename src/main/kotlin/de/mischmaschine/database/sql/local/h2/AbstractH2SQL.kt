package de.mischmaschine.database.sql.local.h2

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.local.AbstractLocalSQL

abstract class AbstractH2SQL(
    user: String,
    password: String,
    databasePath: String
) : AbstractLocalSQL(
    user,
    password,
    databasePath,
    DataBaseType.H2
)