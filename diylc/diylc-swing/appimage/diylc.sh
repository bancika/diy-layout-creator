#!/bin/bash
HERE="$(dirname \"$(readlink -f \"$0\")\")"

# Set JRE path inside AppImage
if [ -d "$HERE/jre" ]; then
    export JAVA_HOME="$HERE/jre"
    export PATH="$JAVA_HOME/bin:$PATH"
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="$(which java)"
fi

cd "$HERE"

exec "$JAVA" \
  -javaagent:"$HERE/../lib/lib/jar-loader.jar" \
  -Xms128m \
  -Xmx4096m \
  -cp "$HERE/../lib/diylc.jar:$HERE/../lib/lib/*" \
  --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED \
  --add-exports java.desktop/com.apple.eio=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  org.diylc.DIYLCStarter "$@"