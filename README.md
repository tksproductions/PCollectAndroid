## PCollect Android

## Setup

Obtain a copy of `keystore1.jks` and the password.

Create `local.properties` based on the sample file:

```
cp local.properties.sample local.properties
```

Set `sdk.dir` to the path to your Android SDK.

## Build and sign

```
./gradlew app:bundle --warning-mode all
jarsigner -keystore  keystore1.jks app/build/outputs/bundle/release/app-release.aab  key0
```
