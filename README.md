AndroidProjectConverter v1.1.0
=======================

This is a command line tool to help convert Android projects from their "old-style" project structure to the project structure that fits Gradle builds and Android Studio.
It is written in Java so it can easily be run in any environment. When executed this program will create a backup of the original project and create a new project that can be used with Gradle and Android Studio.

For more information please see [this website](http://sababado.github.io/AndroidProjectConverter).

##Usage
1. Download the jar from [here](http://sababado.github.io/AndroidProjectConverter/apc.jar).
2. Open a command prompt window and execute as follows:

```shell
java -jar apc.jar "C:/Path/to/original/project" "C:/Path/to/converted/project" "Name"
```

For example: The following command would convert a project called MyCoolApp.
```shell
java -jar apc.jar "C:/android/MyCoolApp" "C:/android/MyCoolAppProject" MyCoolApp
```

##Contributing
This is a maven project, built with Maven 2.2.1
Once the source is downloaded, execute the following from the root directoy:
```shell
mvn clean verify
```
This will run all tests and pull all dependencies required.

##How does this work with a project using Android Libraries?
To reference a library in Andriod Studio, both the library and the project must be imported into Android Studio.

[Referencing `.aar` files isn't yet supported.](https://code.google.com/p/android/issues/detail?id=55863)

##Changelog
###v1.1.0
Added support to modify Module build.gradle file with all dependencies in a lib(s) directory.
###v1.0.0
Initial Release

##Roadmap
1. Implement a feature to check for updates to the `apc.jar` file.
2. Provide another option to specify an Android Test Project and move the contents appropriately to the gradle project.
3. Work with library projects.
