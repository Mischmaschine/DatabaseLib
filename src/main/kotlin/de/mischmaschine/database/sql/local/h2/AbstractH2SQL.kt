package de.mischmaschine.database.sql.local.h2

import de.mischmaschine.database.database.Configuration
import de.mischmaschine.database.database.Database
import de.mischmaschine.database.sql.DataBaseType
import de.mischmaschine.database.sql.local.AbstractLocalSQL

abstract class AbstractH2SQL(
    databasePath: String
) : AbstractLocalSQL(
    Configuration.getUsername(AbstractH2SQL::class),
    Configuration.getPassword(AbstractH2SQL::class),
    databasePath,
    DataBaseType.H2
), Database