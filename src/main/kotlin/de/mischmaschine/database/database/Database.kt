package de.mischmaschine.database.database

import org.slf4j.Logger


/**
 * This class is used as a de.mischmaschine.database.test.main class for all database related classes.
 *
 * **A class cannot be saved in the configuration if it is not extended from this class.**
 */
interface Database {

    var logger: Logger?

}