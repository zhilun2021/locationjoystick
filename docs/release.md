# Release Guide

## Keystore

Generate once, store outside the repo:

```bash
keytool -genkeypair -v \
  -keystore ~/locationjoystick-release.jks \
  -alias locationjoystick \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

Keep `locationjoystick-release.jks` in a secure location (password manager, encrypted backup). Never commit it.

## Environment Variables

Set these before running `make build` or `make bundle`:

```bash
export KEYSTORE_PATH=~/locationjoystick-release.jks
export STORE_PASSWORD=<keystore-password>
export KEY_ALIAS=locationjoystick
export KEY_PASSWORD=<key-password>
```

## Build Outputs

```bash
make build    # APKs → app/build/outputs/apk/release/
make bundle   # AAB  → app/build/outputs/bundle/release/app-release.aab
```

Upload the AAB to Play Store. Use the per-ABI APKs for GitHub Releases.

## Version Bump

Before each release, increment `versionCode` in:

```
build-logic/convention/src/main/kotlin/LjApplicationConventionPlugin.kt
```

`versionCode` must strictly increase with every upload to Play Console.
Also update `VERSION_NAME` (the string shown to users).

## Play Console App Signing

On first upload, enrol in Play App Signing:
- Upload your upload key (the JKS above).
- Google manages the final signing key from that point on.
- Keep the upload key safe — it's used for all future uploads.
