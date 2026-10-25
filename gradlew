#!/usr/bin/env bash
# Bootstrap temporal: descarga Gradle y permite generar el wrapper estándar del proyecto.
set -euo pipefail
GRADLE_VERSION="8.13"
USER_GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
BOOTSTRAP_DIR="$USER_GRADLE_HOME/wim100-bootstrap"
GRADLE_DIR="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION"
ZIP_FILE="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION-bin.zip"
if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  mkdir -p "$BOOTSTRAP_DIR"
  if [ ! -f "$ZIP_FILE" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
    elif command -v wget >/dev/null 2>&1; then
      wget "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -O "$ZIP_FILE"
    else
      echo "Instala curl o wget para descargar Gradle $GRADLE_VERSION." >&2
      exit 1
    fi
  fi
  unzip -q -o "$ZIP_FILE" -d "$BOOTSTRAP_DIR"
fi
exec "$GRADLE_DIR/bin/gradle" "$@"
