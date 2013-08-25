AndroidProjectConverter v1.3.0
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

All jars that are in the project's `libs` directory will automatically be copied and added to the module's `build.gradle` file as `compile` dependencies.

##Usage With Test Projects
This program can convert not only an Android project but also an Android Test Project simultaneously. A path to the test project needs to be specified as an extra argument. For example:
```shell
java -jar apc.jar "C:/android/MyCoolApp" "C:/android/MyCoolAppProject" MyCoolApp "C:/android/MyCoolAppTest"
```
All jars that are in the test project's `libs` directory will automatically be copied and added to the module's `build.gradle` file as `instrumentTestCompile` dependencies.

##Contributing
This is a maven project, built with Maven 2.2.1
Once the source is downloaded, execute the following from the root directory:
```shell
mvn clean verify
```
This will run all tests and pull all dependencies required.

##How does this work with a project using Android Libraries?
To reference a library in Android Studio, both the library and the project must be imported into Android Studio.

[Referencing `.aar` files isn't yet supported.](https://code.google.com/p/android/issues/detail?id=55863)

##Change Log
###v1.3.0
Added the ability to check for updates.
###v1.2.0
Added the ability to copy an Android Test Project simultaneously with an Android Project.
###v1.1.0
Added support to modify Module build.gradle file with all dependencies in a lib(s) directory.
###v1.0.0
Initial Release

##Roadmap
1. Work with library projects.
2. Provide an option to use dependencies from maven central instead of local libs if available.