#!/bin/bash
HERE="$(dirname "$(readlink -f "$0")")"

# Set JRE path inside AppImage
if [ -d "$HERE/../lib/jre" ]; then
    JAVA_HOME="$HERE/../lib/jre"
    PATH="$JAVA_HOME/bin:$PATH"
    export JAVA_HOME PATH
fi

exec "$JAVA_HOME/bin/java" -Xms512m -Xmx4096m -javaagent:lib/jar-loader.jar -Dorg.diylc.scriptRun=true -Dfile.encoding=UTF-8 -cp diylc.jar:lib --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED  --add-exports java.desktop/com.apple.eio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens java.desktop/java.awt.geom=ALL-UNNAMED  org.diylc.DIYLCStarter "$@"