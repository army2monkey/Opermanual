#!/usr/bin/env sh
set -eu
VER=8.13
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/jims-field-playbook"
HOME_DIR="$BASE/gradle-$VER"
ZIP="$BASE/gradle-$VER-bin.zip"
if [ ! -x "$HOME_DIR/bin/gradle" ]; then
  mkdir -p "$BASE"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle $VER..."
    if command -v curl >/dev/null 2>&1; then
      curl -L --fail --retry 3 "https://services.gradle.org/distributions/gradle-$VER-bin.zip" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$VER-bin.zip"
    else
      echo "curl or wget is required for the first build." >&2; exit 1
    fi
  fi
  echo "Extracting Gradle $VER..."
  rm -rf "$HOME_DIR"
  if command -v unzip >/dev/null 2>&1; then unzip -q "$ZIP" -d "$BASE"; else echo "unzip is required." >&2; exit 1; fi
fi
exec "$HOME_DIR/bin/gradle" "$@"
