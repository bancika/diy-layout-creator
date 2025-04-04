#!/usr/bin/env bash
JAVA_BIN="$(which java)"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CLASS_PATH="${DIR}/diylc.jar"

exec java \
    -Xms1024m \
    -Xmx4096m \
    -Dorg.diylc.scriptRun=true \
    -Dfile.encoding=UTF-8 \
    -cp "$CLASS_PATH" \
    --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED \
    --add-exports java.desktop/com.apple.eio=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.text=ALL-UNNAMED \
    --add-opens java.desktop/java.awt=ALL-UNNAMED \
    --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
    --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
    org.diylc.DIYLCStarter "$@"