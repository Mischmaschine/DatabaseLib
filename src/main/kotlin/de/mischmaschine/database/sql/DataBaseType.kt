package de.mischmaschine.database.sql

/**
 * This class is used to resolve the jdbc driver class name for a given database type.
 */
enum class DataBaseType {

    MYSQL, MARIADB, POSTGRESQL, H2, SQLITE

}