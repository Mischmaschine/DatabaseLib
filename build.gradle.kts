import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.dokka") version "1.6.21"
    id("maven-publish")
    kotlin("plugin.serialization") version "1.7.10"
}

group = "de.mischmaschine"

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("com.zaxxer:HikariCP:5.0.1")

    compileOnly("mysql:mysql-connector-java:8.0.30")
    compileOnly("org.postgresql:postgresql:42.5.0")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("org.xerial:sqlite-jdbc:3.39.2.1")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    compileOnly("org.mongodb:mongodb-driver-sync:4.7.1")
    compileOnly("io.lettuce:lettuce-core:6.2.0.RELEASE")

    testImplementation("mysql:mysql-connector-java:8.0.30")
    testImplementation("com.zaxxer:HikariCP:5.0.1")
    testImplementation("io.lettuce:lettuce-core:6.2.0.RELEASE")
    testImplementation("org.mongodb:mongodb-driver-sync:4.7.1")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.10")
    implementation(kotlin("reflect"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

/*tasks.test {
    useJUnitPlatform()
}*/

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.mischmaschine"
            artifactId = "DatabaseLib"
            version = "1.1"

            from(components["java"])
        }
    }
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}