# Android DEX loader sandbox application

## Build

```bash
./gradlew :app-payload:build
cp app-payload/build/outputs/apk/debug/app-payload-debug.apk app-loader/src/main/assets/payload.apk
./gradlew :app-loader:build
```
