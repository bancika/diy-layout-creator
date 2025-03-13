#!/bin/bash
#set -x  # Enable debug mode

HERE="$(dirname "$(readlink -f "$0")")"
echo "diylc.sh: Running from directory: $HERE"

# Check if Java is available
if [ -d "$HERE/jre" ]; then
    export JAVA_HOME="$HERE/jre"
    export PATH="$JAVA_HOME/bin:$PATH"
    JAVA="$JAVA_HOME/bin/java"
    echo "diylc.sh: Using bundled JRE: $JAVA"
else
    JAVA="$(which java)"
    echo "diylc.sh: Using system Java: $JAVA"
fi

# Debug: Check Java existence
if [ ! -x "$JAVA" ]; then
    echo "diylc.sh: ERROR - Java binary not found or not executable!"
    exit 1
fi

cd "$HERE"
echo "diylc.sh: Current working directory: $(pwd)"

# Debug: Print command before execution
echo "diylc.sh: Executing DIYLCStarter with Java"

exec "$JAVA" \
  -Xms512m \
  -Xmx4096m \
  -cp "$HERE/../lib/diylc.jar" \
  --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED \
  --add-exports java.desktop/com.apple.eio=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.text=ALL-UNNAMED \
  --add-opens java.desktop/java.awt=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
  -Dorg.diylc.scriptRun=true \
  org.diylc.DIYLCStarter "$@"
