#!/bin/sh
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed. Open this project in IntelliJ IDEA/Android Studio or install Gradle 8.9+." >&2
exit 1
