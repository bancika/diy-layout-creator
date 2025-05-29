#!/bin/bash
set -x  # Enable debug mode

# Get the AppImage directory
if [ -n "$APPDIR" ]; then
    HERE="$APPDIR/usr/bin"
    JRE_PATH="$APPDIR/usr/lib/jre/bin/java"
    JRE_DIR="$APPDIR/usr/lib/jre"
else
    HERE="$(cd "$(dirname "$0")" && pwd)"
    JRE_PATH="$(dirname "$HERE")/lib/jre/bin/java"
    JRE_DIR="$(dirname "$HERE")/lib/jre"
fi

echo "diylc.sh: Running from directory: $HERE"
echo "diylc.sh: Looking for JRE at: $JRE_PATH"

# List directory contents for debugging
echo "diylc.sh: Contents of $(dirname "$JRE_PATH"):"
ls -la "$(dirname "$JRE_PATH")"

# Use bundled JRE
if [ ! -f "$JRE_PATH" ]; then
    echo "diylc.sh: ERROR - Bundled JRE not found at: $JRE_PATH"
    exit 1
fi

# Check if we're running from a mounted AppImage
if [[ "$HERE" == /tmp/.mount_* ]]; then
    echo "diylc.sh: Running from mounted AppImage"
    # Create a temporary directory for the JRE
    TEMP_DIR="/tmp/diylc-jre-$$"
    mkdir -p "$TEMP_DIR"
    
    # Copy the entire JRE directory to the temporary location
    echo "diylc.sh: Copying JRE to temporary location: $TEMP_DIR"
    cp -r "$JRE_DIR"/* "$TEMP_DIR/"
    chmod -R +x "$TEMP_DIR/bin"
    JRE_PATH="$TEMP_DIR/bin/java"
    
    # Clean up the temporary directory on exit
    trap 'rm -rf "$TEMP_DIR"' EXIT
fi

# Verify permissions
echo "diylc.sh: Verifying JRE permissions..."
ls -la "$JRE_PATH"

if [ ! -x "$JRE_PATH" ]; then
    echo "diylc.sh: ERROR - JRE is not executable at: $JRE_PATH"
    echo "diylc.sh: Current permissions: $(ls -l "$JRE_PATH")"
    exit 1
fi

echo "diylc.sh: Using JRE: $JRE_PATH"

cd "$HERE"
echo "diylc.sh: Current working directory: $(pwd)"

# Debug: Print command before execution
echo "diylc.sh: Executing DIYLCStarter with Java"

# Execute Java directly with the command array
exec "$JRE_PATH" \
    -Xms1024m \
    -Xmx4096m \
    -cp "$(dirname "$HERE")/lib/diylc.jar" \
    --add-opens java.base/java.util=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.text=ALL-UNNAMED \
    --add-opens java.desktop/java.awt=ALL-UNNAMED \
    --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
    --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
    -Dorg.diylc.scriptRun=true \
    org.diylc.DIYLCStarter "$@"

