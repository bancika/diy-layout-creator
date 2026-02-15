#!/bin/bash

# Print release notes from org.diylc.utils.VersionReader (run after build).

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="${PROJECT_DIR}/diylc-swing/target/diylc.jar"

if [ ! -f "${JAR}" ]; then
    exit 0
fi

RAW=$(java -cp "${JAR}" \
    --add-opens java.base/java.util=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.text=ALL-UNNAMED \
    --add-opens java.desktop/java.awt=ALL-UNNAMED \
    --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
    --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
    org.diylc.utils.VersionReader "${PROJECT_DIR}" 2>/dev/null) || true

if [ -z "${RAW}" ]; then
    exit 0
fi

# VersionReader prints Markdown changelog lines, then blank line, then HTML — keep only Markdown
echo "${RAW}" | sed '/^[[:space:]]*</,$d'
