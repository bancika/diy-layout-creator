#!/bin/bash

# Draft a GitHub release and upload build artifacts from the deploy folder.
# Requires: GitHub CLI (gh) installed and authenticated.
#
# Usage:
#   ./draft-github-release.sh                    # use version from pom.xml, deploy/
#   ./draft-github-release.sh v1.2.3             # specific tag
#   ./draft-github-release.sh v1.2.3 /path/to/deploy
#
# Prerequisites:
#   - Install: https://cli.github.com/
#   - Login:   gh auth login

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${2:-${PROJECT_DIR}/deploy}"
TAG_OR_VERSION="${1:-}"

# Resolve version from parent pom.xml if not provided
if [ -z "${TAG_OR_VERSION}" ]; then
    if [ -f "${PROJECT_DIR}/pom.xml" ]; then
        VERSION=$(sed -n 's/.*<version>\([^<]*\)<\/version>.*/\1/p' "${PROJECT_DIR}/pom.xml" | head -1)
        TAG_OR_VERSION="v${VERSION}"
    else
        echo "ERROR: No tag/version given and could not read pom.xml"
        exit 1
    fi
fi

# Ensure tag has v prefix for display
TAG="${TAG_OR_VERSION}"
[[ "${TAG}" != v* ]] && TAG="v${TAG}"

echo "=========================================="
echo "Draft GitHub release and upload artifacts"
echo "=========================================="
echo "Tag:        ${TAG}"
echo "Deploy dir: ${DEPLOY_DIR}"
echo ""

if ! command -v gh &>/dev/null; then
    echo "ERROR: GitHub CLI (gh) is not installed."
    echo "Install: https://cli.github.com/"
    exit 1
fi

if ! gh auth status &>/dev/null; then
    echo "ERROR: Not logged in to GitHub CLI. Run: gh auth login"
    exit 1
fi

if [ ! -d "${DEPLOY_DIR}" ]; then
    echo "ERROR: Deploy directory does not exist: ${DEPLOY_DIR}"
    echo "Run ./build-all-profiles.sh first (or pass the deploy path as second argument)."
    exit 1
fi

# Collect zip and exe files; skip *intel.zip and *silicon.zip (keep unsigned Mac zips only)
shopt -s nullglob
ZIPS=("${DEPLOY_DIR}"/*.zip)
EXES=("${DEPLOY_DIR}"/*.exe)
ARTIFACTS=()
for f in "${ZIPS[@]}" "${EXES[@]}"; do
    name=$(basename "$f")
    if [[ "$name" == *intel.zip || "$name" == *silicon.zip ]]; then
        continue
    fi
    ARTIFACTS+=("$f")
done

if [ ${#ARTIFACTS[@]} -eq 0 ]; then
    echo "ERROR: No .zip or .exe files found in ${DEPLOY_DIR} (after excluding *intel.zip and *silicon.zip)"
    exit 1
fi

echo "Artifacts to upload (${#ARTIFACTS[@]}):"
for f in "${ARTIFACTS[@]}"; do
    echo "  - $(basename "$f")"
done
echo ""

# Generate release notes using print_version.sh
NOTES_FILE="${PROJECT_DIR}/.release-notes-$$.md"
trap 'rm -f "${NOTES_FILE}"' EXIT
if [ -f "${PROJECT_DIR}/print_version.sh" ]; then
    bash "${PROJECT_DIR}/print_version.sh" > "${NOTES_FILE}" || true
fi
if [ ! -s "${NOTES_FILE}" ]; then
    echo "Release ${TAG}. Edit this description on GitHub before publishing." > "${NOTES_FILE}"
fi
echo "Using release notes from print_version.sh"
echo ""

# Create draft release and upload assets in one go
# --draft: create as draft so you can edit/publish from the GitHub UI
gh release create "${TAG}" \
    --draft \
    --title "${TAG}" \
    --notes-file "${NOTES_FILE}" \
    "${ARTIFACTS[@]}"

echo ""
echo "Done. Open the draft release on GitHub to edit notes and publish:"
echo "  gh release view ${TAG} --web"
echo ""
