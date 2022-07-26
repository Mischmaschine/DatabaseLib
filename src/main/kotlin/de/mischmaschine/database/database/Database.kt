package de.mischmaschine.database.database

import java.util.logging.Logger


/**
 * This class is used as a main class for all database related classes.
 *
 * **A class cannot be saved in the configuration if it is not extended from this class.**
 */
interface Database {

    val logger: Logger
        get() = Logger.getLogger(this::class.simpleName)

}