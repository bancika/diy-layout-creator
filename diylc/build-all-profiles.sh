#!/bin/bash

# Script to build all Maven profiles and copy zip files to deployment folder
# Usage: ./build-all-profiles.sh [to-deploy-folder]

set -e  # Exit on error

# Parse arguments
DEPLOY_DIR="${1:-}"

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SWING_MODULE_DIR="${PROJECT_DIR}/diylc-swing"
DEPLOY_DIR="${DEPLOY_DIR:-${PROJECT_DIR}/deploy}"

# List of profiles to build
PROFILES=(
    "universal-zip"
    "linux-appimage"
    "win64-zip"
    "win64-installer"
    "mac-x86"
    "mac-arm"
)

echo "=========================================="
echo "Building all Maven profiles"
echo "=========================================="
echo "Project directory: ${PROJECT_DIR}"
echo "Swing module directory: ${SWING_MODULE_DIR}"
echo "Deployment directory: ${DEPLOY_DIR}"
echo "Tests: Skipped"
echo ""

# Create deployment directory if it doesn't exist
mkdir -p "${DEPLOY_DIR}"

# Change to project root directory
cd "${PROJECT_DIR}"

# Build each profile (clean before each one)
TARGET_DIR="${SWING_MODULE_DIR}/target"
TOTAL_ZIP_COUNT=0

for profile in "${PROFILES[@]}"; do
    echo "----------------------------------------"
    echo "Building profile: ${profile}"
    echo "----------------------------------------"
    
    # Clean and build from root, specifying the swing module and profile
    # This ensures dependencies (diylc-core, diylc-library) are built first
    mvn clean package -pl diylc-swing -am -P"${profile}" -DskipTests || {
        echo "ERROR: Failed to build profile ${profile}"
        echo "Continuing with next profile..."
        continue
    }
    
    echo "✓ Profile ${profile} built successfully"
    
    # Copy zip files immediately after each build (before next clean)
    if [ -d "${TARGET_DIR}" ]; then
        echo "Copying zip files from ${profile}..."
        while IFS= read -r -d '' zipfile; do
            filename=$(basename "${zipfile}")
            dest="${DEPLOY_DIR}/${filename}"
            
            echo "  → ${filename}"
            cp "${zipfile}" "${dest}"
            TOTAL_ZIP_COUNT=$((TOTAL_ZIP_COUNT + 1))
        done < <(find "${TARGET_DIR}" -maxdepth 1 -type f -name "*.zip" -print0 2>/dev/null)
    fi
    
    echo ""
done

echo "=========================================="
echo "Copying summary"
echo "=========================================="
if [ ${TOTAL_ZIP_COUNT} -eq 0 ]; then
    echo "No zip files were copied"
else
    echo "✓ Copied ${TOTAL_ZIP_COUNT} zip file(s) total to ${DEPLOY_DIR}"
fi

echo ""
echo "=========================================="
echo "Build and deployment complete!"
echo "=========================================="
echo "Deployment folder: ${DEPLOY_DIR}"
echo "Files:"
ls -lh "${DEPLOY_DIR}"/*.zip 2>/dev/null || echo "  (no zip files found)"

