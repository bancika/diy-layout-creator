#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set DEBUG environment variable
export DEBUG=true

# Build classpath from lib and library directories
CLASSPATH=""
for jar in "$SCRIPT_DIR/../lib"/*.jar "$SCRIPT_DIR/../library"/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Remove leading colon
CLASSPATH="${CLASSPATH#:}"

# Run the VersionReader class
java -cp "$CLASSPATH" org.diylc.utils.VersionReader "$SCRIPT_DIR/../" 