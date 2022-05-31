package de.mischmaschine.database.sql.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Column(
    val maxLength: Int,
    val nullable: Boolean = true,
)
