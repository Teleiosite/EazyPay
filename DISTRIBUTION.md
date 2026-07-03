# EazyPay Distribution Guide

This repository is ready to share as a **demo/prototype Android app**. It is not approved for real-money payment processing.

## Quick start for reviewers

1. Install Android Studio or use a JDK 17-capable Android build environment.
2. Clone the repository.
3. Build the debug APK:

   ```bash
   ./gradlew assembleDebug
   ```

4. Share or install the generated APK:

   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Release APK signing

For a signed release build, create a private upload keystore outside of git and provide the signing environment variables at build time:

```bash
export KEYSTORE_PATH=/absolute/path/to/upload-key.jks
export STORE_PASSWORD='replace-me'
export KEY_ALIAS='upload'
export KEY_PASSWORD='replace-me'
./gradlew assembleRelease
```

If these variables are not present, `assembleRelease` creates an unsigned release artifact that must be signed before distribution.

## Distribution safety notes

- Use the debug APK only for demos, internal review, and stakeholder walkthroughs.
- Do not advertise this app as a live wallet or payment processor.
- Do not collect real student/vendor credentials, real bank information, or live payment card data.
- Complete the backend ledger, NFC/tokenization, admin operations, compliance, and security work documented in `docs/production-readiness-audit.md` before any live-money pilot.
