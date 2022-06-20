import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.dokka") version "1.6.21"
}

group = "de.mischmaschine"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("org.postgresql:postgresql:42.3.4")
    implementation("com.h2database:h2:2.1.212")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.4")
    implementation("org.mongodb:mongodb-driver-sync:4.6.0")
    implementation("org.mongodb:mongodb-driver-sync:4.6.0")

    implementation("com.google.code.gson:gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.21")
    //implementation("com.esotericsoftware:reflectasm:1.11.9")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}