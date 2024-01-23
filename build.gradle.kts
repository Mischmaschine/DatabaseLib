import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.dokka") version "1.9.10"
    id("maven-publish")
    kotlin("plugin.serialization") version "1.9.21"
}

group = "de.mischmaschine"

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {

    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("mysql:mysql-connector-java:8.0.33")
    compileOnly("org.postgresql:postgresql:42.7.1")
    compileOnly("com.h2database:h2:2.2.224")
    compileOnly("org.xerial:sqlite-jdbc:3.45.0.0")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    compileOnly("org.mongodb:mongodb-driver-sync:4.11.1")
    compileOnly("io.lettuce:lettuce-core:6.3.1.RELEASE")

    testImplementation("mysql:mysql-connector-java:8.0.33")
    testImplementation("com.zaxxer:HikariCP:5.1.0")
    testImplementation("io.lettuce:lettuce-core:6.3.1.RELEASE")
    testImplementation("org.mongodb:mongodb-driver-sync:4.11.1")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.12.0")
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")
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