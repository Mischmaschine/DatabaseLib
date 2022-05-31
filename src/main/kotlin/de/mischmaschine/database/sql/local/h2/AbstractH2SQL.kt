package de.mischmaschine.database.sql.local.h2

import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.configuration.SQLConfiguration
import de.mischmaschine.database.sql.local.AbstractLocalSQL

abstract class AbstractH2SQL(
    databasePath: String
) : AbstractLocalSQL(
    SQLConfiguration.getUsername(),
    SQLConfiguration.getPassword(),
    databasePath,
    DataBaseType.H2
)