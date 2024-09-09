# How to test?

## Build the gradle plugin
```bash
./gradlew :plugin-build:publish
cd extension/src/main/cpp
conan install . --profile android_armv8 --install-folder=../../../build/conan_build_arm64-v8a -u --build=missing
cd -
./gradlew publish
```
The artifact will be pushed to your mavenLocal repository.

## Integrate the plugin in your project
https://github.com/jxw1102/TommyMap/commit/4ff591b627cba095056e5f346d6afe31718a109d

## Run the demo directly
Checkout `extension-test` branch of this repo: https://github.com/jxw1102/TommyMap then build and run.
