#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# FTP settings
FTP_USER="diylc_deploy@diy-fever.com"
FTP_HOST="ftp.diy-fever.com"

# Check if password is set
if [ -z "$DIYLC_DEPLOY_PWD" ]; then
    echo "Error: DIYLC_DEPLOY_PWD environment variable is not set"
    exit 1
fi

# Source file path
SOURCE_FILE="$SCRIPT_DIR/../../diylc-core/src/main/resources/update.xml"

# Check if source file exists
if [ ! -f "$SOURCE_FILE" ]; then
    echo "Error: update.xml not found at $SOURCE_FILE"
    exit 1
fi

# Upload using FTP
echo "Uploading update.xml to $FTP_USER@$FTP_HOST..."
ftp -n "$FTP_HOST" << EOF
user $FTP_USER $DIYLC_DEPLOY_PWD
binary
put "$SOURCE_FILE" update.xml
quit
EOF

# Check FTP exit status
if [ $? -eq 0 ]; then
    echo "Upload successful"
else
    echo "Upload failed"
    exit 1
fi 
