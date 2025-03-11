#!/bin/bash

echo "==================================================="
echo "DIYLC Maven Local Dependency Installer for Unix"
echo "==================================================="
echo
echo "This script will install all JAR files from the lib directories"
echo "to your local Maven repository."
echo

# Define the group ID for all local dependencies
GROUP_ID="org.diylc.local"

# List of module lib directories to process
MODULES=("diylc-swing")

echo "Processing dependencies..."
echo

for module in "${MODULES[@]}"; do
    echo "Module: $module"
    echo "-----------------------"
    
    if [ -d "$module/lib" ]; then
        for jar_file in "$module/lib"/*.jar; do
            # Skip if no jar files found
            [ -e "$jar_file" ] || continue
            
            # Extract filename without extension
            ARTIFACT_ID=$(basename "$jar_file" .jar)
            
            echo "Installing $ARTIFACT_ID.jar"
            
            # Install the JAR to the local Maven repository
            mvn install:install-file \
                -Dfile="$jar_file" \
                -DgroupId="$GROUP_ID" \
                -DartifactId="$ARTIFACT_ID" \
                -Dversion=1.0 \
                -Dpackaging=jar \
                -DgeneratePom=true \
                -DcreateChecksum=true
                
            echo
        done
    else
        echo "No lib directory found for $module"
        echo
    fi
done

echo "==================================================="
echo "All dependencies have been installed!"
echo
echo "You can now reference these dependencies in your POM files as:"
echo
echo "<dependency>"
echo "    <groupId>$GROUP_ID</groupId>"
echo "    <artifactId>ARTIFACT_NAME</artifactId>"
echo "    <version>1.0</version>"
echo "</dependency>"
echo "==================================================="
