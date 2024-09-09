# How to test?

## Build the gradle plugin
```bash
./gradlew :plugin-build:publish
./gradlew publish
```
The artifact will be pushed to your mavenLocal repository.

## Integrate the plugin in your project
0) Add `mavenLocal()` in repositories of both pluginManagement and dependencyResolutionManagement.
This project is not published.

1) Setting up the extension. 
In `build.gradle` file:
```kotlin
plugins {
    id("com.tomtom.sdk.extension") version "0.0.1"
}

dependencies {
    implementation("com.tomtom.sdk.extension:library:0.0.1")
}
```

2) Configure the extension.
- Demo of Java/Kotlin modification
```kotlin
navSdkExtension {
    shouldDisableInertia = true
}
```

3) Use the extension.
- Demo of C++ function hook
```kotlin
NavSdkExtension().hookSetStyle()
```
- Demo of standalone new feature
```kotlin
IncidentDetailApi().getIncidentDetail(listOf("id-xxx-xxx"), "zh-TW", TOMTOM_API_KEY)
```

## Run the demo directly
Checkout `extension-test` branch of this repo: https://github.com/jxw1102/TommyMap then build and run.
