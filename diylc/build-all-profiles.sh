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
    
    # Copy zip and exe files immediately after each build (before next clean)
    if [ -d "${TARGET_DIR}" ]; then
        echo "Looking for artifacts in ${TARGET_DIR}..."
        PROFILE_FILE_COUNT=0
        
        # Find all zip files in target directory (excluding DIYLC-notarize.zip)
        while IFS= read -r -d '' zipfile; do
            filename=$(basename "${zipfile}")
            # Skip DIYLC-notarize.zip files
            if [[ "${filename}" != "DIYLC-notarize.zip" ]] && [ -f "${zipfile}" ]; then
                dest="${DEPLOY_DIR}/${filename}"
                
                echo "  → Copying: ${filename}"
                cp "${zipfile}" "${dest}"
                TOTAL_ZIP_COUNT=$((TOTAL_ZIP_COUNT + 1))
                PROFILE_FILE_COUNT=$((PROFILE_FILE_COUNT + 1))
            fi
        done < <(find "${TARGET_DIR}" -maxdepth 1 -type f -name "*.zip" -print0 2>/dev/null)
        
        # Find all exe files in target directory
        while IFS= read -r -d '' exefile; do
            if [ -f "${exefile}" ]; then
                filename=$(basename "${exefile}")
                dest="${DEPLOY_DIR}/${filename}"
                
                echo "  → Copying: ${filename}"
                cp "${exefile}" "${dest}"
                TOTAL_ZIP_COUNT=$((TOTAL_ZIP_COUNT + 1))
                PROFILE_FILE_COUNT=$((PROFILE_FILE_COUNT + 1))
            fi
        done < <(find "${TARGET_DIR}" -maxdepth 1 -type f -name "*.exe" -print0 2>/dev/null)
        
        if [ ${PROFILE_FILE_COUNT} -eq 0 ]; then
            echo "  ⚠ Warning: No artifacts found for profile ${profile}"
            # List what's actually in the target directory for debugging
            echo "  Debug: Files in target directory:"
            ls -la "${TARGET_DIR}"/*.{zip,exe} 2>/dev/null | head -5 || echo "    (no zip/exe files found)"
        else
            echo "  ✓ Copied ${PROFILE_FILE_COUNT} file(s) from ${profile}"
        fi
    else
        echo "  ⚠ Warning: Target directory ${TARGET_DIR} does not exist"
    fi
    
    echo ""
done

echo "=========================================="
echo "Copying summary"
echo "=========================================="
if [ ${TOTAL_ZIP_COUNT} -eq 0 ]; then
    echo "No artifacts were copied"
else
    echo "✓ Copied ${TOTAL_ZIP_COUNT} file(s) total to ${DEPLOY_DIR}"
fi

echo ""
echo "=========================================="
echo "Build and deployment complete!"
echo "=========================================="
echo "Deployment folder: ${DEPLOY_DIR}"
echo "Files:"
ls -lh "${DEPLOY_DIR}"/*.{zip,exe} 2>/dev/null || echo "  (no zip/exe files found)"

