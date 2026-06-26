#!/usr/bin/env bash

# Find the project root
APP_HOME=$(dirname "$0")
if [ -z "$APP_HOME" ]; then
  APP_HOME="."
fi

# Define the jar path
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Auto-download the wrapper jar if it doesn't exist
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -L -s -o "$WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar"
    else
        echo "Error: Neither curl nor wget was found. Cannot download gradle-wrapper.jar."
        exit 1
    fi
fi

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        # Only use JAVA_HOME if it contains as executable java
        JAVACMD="$JAVA_HOME/bin/java"
    fi
fi

if [ -z "$JAVACMD" ] ; then
    JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "Error: JAVA_HOME is not defined and no 'java' command could be found in your PATH."
    exit 1
fi

# Execute gradle-wrapper
exec "$JAVACMD" -Xmx4g -Dfile.encoding=UTF-8 -jar "$WRAPPER_JAR" "$@"
