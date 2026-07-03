#!/usr/bin/env bash
set -euo pipefail

# Builds the APK intended for internal sharing/review.
# This project is a demo prototype; do not use this artifact for live payments.

if [[ -z "${JAVA_HOME:-}" ]]; then
  if command -v mise >/dev/null 2>&1 && mise where java@17.0.2 >/dev/null 2>&1; then
    export JAVA_HOME="$(mise where java@17.0.2)"
  fi
fi

if [[ -x ./gradlew ]]; then
  ./gradlew clean assembleDebug
else
  gradle clean assembleDebug
fi

APK="app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "$APK" ]]; then
  echo "Expected APK not found: $APK" >&2
  exit 1
fi

mkdir -p dist
cp "$APK" "dist/EazyPay-demo-debug.apk"
sha256sum "dist/EazyPay-demo-debug.apk" > "dist/EazyPay-demo-debug.apk.sha256"

echo "Created dist/EazyPay-demo-debug.apk"
echo "Created dist/EazyPay-demo-debug.apk.sha256"
