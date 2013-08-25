AndroidProjectConverter
=======================
v1.0.0
----------

##Overview
This is a command line tool to help convert Android projects from their "old-style" project structure to the project structure that fits Gradle builds and Android Studio.
It is written in Java so it can easily be run in any environment. When executed this program will create a backup of the original project and create a new project that can be used with Gradle and Android Studio.

##Usage
1. Download the jar from [here](http://google.com).
2. Open a command prompt window and execute as follows
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

##Roadmap
1. Implement a feature to check for updates to the `apc.jar` file.
2. Provide another option to specify an Android Test Project and move the contents appropriately to the gradle project.