import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

group = "de.mischmaschine"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.6.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("org.postgresql:postgresql:42.3.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

/*tasks.test {
    useJUnitPlatform()
}*/

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}