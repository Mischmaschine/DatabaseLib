import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.dokka") version "1.6.21"
    id("maven-publish")
}

group = "de.mischmaschine"

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("com.zaxxer:HikariCP:5.0.1")

    compileOnly("mysql:mysql-connector-java:8.0.29")
    compileOnly("org.postgresql:postgresql:42.3.6")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("org.xerial:sqlite-jdbc:3.36.0.3")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.0.5")
    compileOnly("org.mongodb:mongodb-driver-sync:4.6.0")
    compileOnly("org.redisson:redisson:3.17.5")

    testImplementation("org.redisson:redisson:3.17.5")
    testImplementation("org.mongodb:mongodb-driver-sync:4.6.0")

    implementation("com.google.code.gson:gson:2.9.0")
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
            version = "1.0"

            from(components["java"])
        }
    }
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}