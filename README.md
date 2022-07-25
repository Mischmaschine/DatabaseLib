# DatabaseLib

    This project is a library for working with databases.
    It supports MariaDB, MySQL, PostgreSQL, H2SQL, SQLite MongoDB and Redis.

## Import

### Maven

#### <b><u>Step 1. Add the repository</u></b>

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

#### <b><u>Step 2. Add the dependency</u></b>

```xml

<dependency>
    <groupId>com.github.Mischmaschine</groupId>
    <artifactId>DatabaseLib</artifactId>
    <version>Tag</version>
</dependency>
```

<br/>

### Gradle - Groovy

#### <b><u>Step 1. Add the repository</u></b>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

#### <b><u>Step 2. Add the dependency</u></b>

```groovy
dependencies {
    implementation 'com.github.Mischmaschine:DatabaseLib:master-SNAPSHOT'
}
```

<br/>

### Gradle - kts

<b><u>Step 1. Add the repository</u></b>

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

<b><u>Step 2. Add the dependency</u></b>

```kotlin
dependencies {
    implementation("com.github.Mischmaschine:DatabaseLib:master-SNAPSHOT")
}
```

## Usage

### Set the connection credentials

#### Kotlin

```kotlin
import de.mischmaschine.database.database.configuration.Configuration

Configuration("host", port, "username", "password", AbstractMySQL::class)
```

#### Java

```java
import de.mischmaschine.database.database.configuration.Configuration;

new Configuration("host", port, "username", "password", AbstractMySQL.class);
```

## Contributing

Please feel free to fork this project and make a pull request.

## License

This project is licensed under the LGPL-2.1 license.

<a href="https://www.gnu.org/licenses/lgpl-2.1.html">
      <img src="https://img.shields.io/badge/License-LGPL%202.1-blue.svg" alt="License LGPL-2.1" />
</a>

### Version

```
$ git describe --tags --always
```

### Changelog

```
$ git log --oneline --decorate --graph --all --color=always
```

### Contributors

```
$ git shortlog -sn --all
```

  
