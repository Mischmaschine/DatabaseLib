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
    <version>master-SNAPSHOT</version>
</dependency>
```

<br/>

### Gradle - Groovy

#### <b><u>Step 1. Add the repository</u></b>

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
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
import de.mischmaschine.database.configuration.Configuration

Configuration("host", port, "username", "password", AbstractRedis::class)
```

#### Java

```java
import de.mischmaschine.database.configuration.Configuration;

new Configuration("host",port,"username","password",AbstractRedis.class);
```

<br>

### Set up the connection to your individual database

#### Kotlin

```kotlin
import de.mischmaschine.database.redis.AbstractRedis;

//Create a class which extends AbstractRedis
class Redis : AbstractRedis(database = 0, ssl = false, logging = true)

//Go to your main class and create an instance of your database class
main {
    //Create an instance of your class
    //This will automatically connect to the database
    val redis = Redis()

    //Do something with the database
    with(redis) {

        //Set a value in the database with the key "key" and the value "value"
        //Returns a FutureAction out of a Unit (void) type to check when the action is completed
        updateKeyAsync("key", "value")

        //Get the value of the key "key"
        //This will return a FutureAction which will be completed with the value. 
        //Use .onSuccess to get the value if present
        //Use .onFailure to get the exception if present
        getValueAsync("key")
    }

    //Disconnect from the database
    redis.shutdown()
}
```

#### Java

```java
import de.mischmaschine.database.redis.AbstractRedis;

//Create a class which extends AbstractRedis
public class Redis extends AbstractRedis(){

public Redis(){
        super(0,false,true);
        }
        }

public class Main {

    public static void main(String[] args) {
        //Create an instance of your class
        //This will automatically connect to the database
        Redis redis = new Redis();

        //Set a value in the database with the key "key" and the value "value"
        //Returns a FutureAction out of a Unit (void) type to check when the action is completed
        redis.updateKeyAsync("key", "value");

        //Get the value of the key "key"
        //This will return a FutureAction which will be completed with the value. 
        //Use .onSuccess to get the value if present
        //Use .onFailure to get the exception if present
        redis.getValueAsync("key");


        redis.shutdown();
    }
}
```

This is an example for Redis. You can use the process for all other databases like AbstractMongoDB.

### Redis-PubSub Example

#### Kotlin

```kotlin
import de.mischmaschine.database.redis.AbstractRedis

main {

    val redis = ...
    redis.subscribe("channel") { channel, message ->
        //In this case, the message would be "Hello World"
        println("Received message: $message")
    }

    //This will publish to the channel "channel" the message "Hello World!"
    redis.publish("channel", "Hello World!")


    //If you no longer want to subscribe to the channel "channel", you can unsubscribe
    redis.unsubscribe("channel")

    //Shutdown this client and close all open connections once this method is called. Once all connections are closed, the associated ClientResources are shut down/released gracefully considering quiet time and the shutdown timeout
    redis.shutdown()
}

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

  
