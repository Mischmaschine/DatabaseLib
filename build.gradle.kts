import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.dokka") version "1.6.21"
    id("maven-publish")
    kotlin("plugin.serialization") version "1.8.22"
}

group = "de.mischmaschine"

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("com.zaxxer:HikariCP:5.0.1")

    compileOnly("mysql:mysql-connector-java:8.0.33")
    compileOnly("org.postgresql:postgresql:42.6.0")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    compileOnly("org.mongodb:mongodb-driver-sync:4.10.1")
    compileOnly("io.lettuce:lettuce-core:6.2.4.RELEASE")

    testImplementation("mysql:mysql-connector-java:8.0.33")
    testImplementation("com.zaxxer:HikariCP:5.0.1")
    testImplementation("io.lettuce:lettuce-core:6.2.2.RELEASE")
    testImplementation("org.mongodb:mongodb-driver-sync:4.8.1")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.9.0")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.mischmaschine"
            artifactId = "DatabaseLib"
            version = "1.2"

            from(components["java"])
        }
    }
}

val targetCompatibility = JavaVersion.VERSION_17.toString()

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = targetCompatibility
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = targetCompatibility
}


kotlin {
    jvmToolchain(11)
}