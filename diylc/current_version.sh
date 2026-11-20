#!/bin/bash

# Print the newest version number in update.xml, e.g. 6.11.0. This is the version the application
# reports at runtime, and the build scripts pass it to Maven as -Drevision so packages are named
# after it.

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UPDATE_XML="${PROJECT_DIR}/diylc-core/src/main/resources/update.xml"

VERSION=$(awk '
    /<versionNumber>/ { major = ""; minor = ""; build = "" }
    /<major>/ { gsub(/[^0-9]/, ""); major = $0 }
    /<minor>/ { gsub(/[^0-9]/, ""); minor = $0 }
    /<build>/ { gsub(/[^0-9]/, ""); build = $0 }
    /<\/versionNumber>/ { print major "." minor "." build }
' "${UPDATE_XML}" | sort -t. -k1,1n -k2,2n -k3,3n | tail -1)

if [ -z "${VERSION}" ]; then
    echo "ERROR: Could not read a version from ${UPDATE_XML}" >&2
    exit 1
fi

echo "${VERSION}"
