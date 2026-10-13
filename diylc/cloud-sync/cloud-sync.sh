#!/bin/sh
# Syncs the pages listed in cloud-sync.json to the DIYLC cloud. Pass --dry-run to download and
# render thumbnails without uploading. Uses the jar built by "mvn package" unless DIYLC_JAR is set.
DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="${DIYLC_JAR:-$DIR/../diylc-swing/target/diylc.jar}"
exec java -Xmx2g -Djava.awt.headless=true -Dlog4j.defaultInitOverride=true \
  --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
  -cp "$JAR" org.diylc.swing.plugins.cloud.sync.CloudSyncJob "$DIR/cloud-sync.json" "$@"
