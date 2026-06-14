#!/bin/sh
# Wrapper for the AI assistant (opencode) to build/test the project.
# Sets the flatpak Android Studio environment before running Gradle.
export JAVA_HOME=/app/extra/jbr
export ANDROID_HOME=/home/grey/Android/Sdk
export PATH=$JAVA_HOME/bin:$PATH

exec "$(dirname "$0")/../gradlew" "$@"
