#!/bin/sh
#
# Gradle wrapper script for Unix
#

# Attempt to set APP_HOME
PRGDIR=$(dirname "$0")
APP_HOME=$(cd "$PRGDIR" >/dev/null && pwd)

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Java opts
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# OS-specific setup
case "$(uname)" in
  CYGWIN*|MINGW*|MSYS*)
    APP_HOME=$(cygpath --path --mixed "$APP_HOME")
    CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")
    ;;
esac

JAVA_EXE=java
if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

exec "$JAVA_EXE" $DEFAULT_JVM_OPTS \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
