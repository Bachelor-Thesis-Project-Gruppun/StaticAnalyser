# This is a guide on how to import this plugin in another project.

## If importing locally
This part is for when you want to import this project from you local maven repository.

### Build and push this project to you local maven repository, 
this can be done simply using the command:
```bash
gradle publishToMavenLocal
```

### Import the project.
This is done by adding this buildscript block to the top of your `build.gradle` file:
```groovy
buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        classpath group: 'com.github.gruppun', name: 'staticanalyser', version: '1.0'
    }
}

apply plugin: "StaticAnalyser"
```
***Note: replace the version with the desired version, if unsure use the latest version.***

To be able to import files such as our annotations or enums add the following line the projects dependencies block:
***NOTE, not the dependencies block in the buildscript such as the one described above!***
 ```groovy
    implementation 'com.github.gruppun:staticanalyser:1.1'
```
***Note: Once more, replace the version as described above***