#!/bin/sh
DIR="$(dirname "$(readlink -f "$0")")/../diylc"
export JAVA_HOME="$DIR/jre"
exec "$JAVA_HOME/bin/java" -Djava.awt.headless=false -jar "$DIR/diylc.jar"