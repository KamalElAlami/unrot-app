#!/usr/bin/env bash
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"

cd "$(dirname "$0")"
./gradlew testDebugUnitTest assembleDebug

echo "APK: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
