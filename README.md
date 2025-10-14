# Android DEX loader sandbox application

## Developer environment setup

Put into `local.properties` file:

```properties
PAYLOAD_STORE_FILE=./payload-keystore.jks
PAYLOAD_STORE_PASSWORD=123456
PAYLOAD_KEY_ALIAS=payload_key
PAYLOAD_KEY_PASSWORD=123456
```

Generate keystore:

```bash
./app-payload/bin/gen-keystore.sh
```

Export public key:

```bash
./app-payload/bin/export-public-key.sh
```

Copy public key inside the loader app:

```bash
cp public_key.cer app-loader/src/main/assets/public_key.cer
```


## Build

### v1. Build and deploy a properly signed release APK

```bash
./gradlew :app-payload:assembleRelease
cp app-payload/build/outputs/apk/release/app-payload-release.apk app-loader/src/main/assets/payload.apk
./gradlew :app-loader:build
```

### v2. Build and deploy unsigned debug APK (loading will fail)

```bash
./gradlew :app-payload:assembleDebug
cp app-payload/build/outputs/apk/debug/app-payload-debug.apk app-loader/src/main/assets/payload.apk
./gradlew :app-loader:build
```
