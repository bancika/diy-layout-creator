#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set DEBUG environment variable
export DEBUG=true

# Run the VersionReader class
java -cp "../target/diylc.jar" --add-opens java.base/java.util=ALL-UNNAMED \
                                   --add-opens java.base/java.lang=ALL-UNNAMED \
                                   --add-opens java.base/java.text=ALL-UNNAMED \
                                   --add-opens java.desktop/java.awt=ALL-UNNAMED \
                                   --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
                                   --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
                                   org.diylc.utils.VersionReader "$SCRIPT_DIR/../"
