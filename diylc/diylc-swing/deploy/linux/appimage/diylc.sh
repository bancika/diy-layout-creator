#!/bin/bash
#set -x  # Enable debug mode

# Get the AppImage directory
if [ -n "$APPDIR" ]; then
    HERE="$APPDIR/usr/bin"
else
    HERE="$(cd "$(dirname "$0")" && pwd)"
fi

echo "diylc.sh: Running from directory: $HERE"

# Use system Java
JAVA="$(which java)"
if [ -z "$JAVA" ] || [ ! -x "$JAVA" ]; then
    echo "diylc.sh: ERROR - System Java not found or not executable!"
    echo "diylc.sh: Please install Java 17 or later"
    exit 1
fi

echo "diylc.sh: Using system Java: $JAVA"

cd "$HERE"
echo "diylc.sh: Current working directory: $(pwd)"

# Debug: Print command before execution
echo "diylc.sh: Executing DIYLCStarter with Java"

exec "$JAVA" \
  -Xms1024m \
  -Xmx4096m \
  -cp "$APPDIR/usr/lib/diylc.jar" \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.text=ALL-UNNAMED \
  --add-opens java.desktop/java.awt=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
  -Dorg.diylc.scriptRun=true \
  org.diylc.DIYLCStarter "$@"
