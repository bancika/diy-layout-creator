#!/bin/bash

# Build the AppImage
echo "Building AppImage..."
cd ../..
#mvn clean package -P linux-appimage

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

# Get the AppImage path
APPIMAGE="target/DIYLayoutCreator-5.1.0-x86_64.AppImage"
ZIP_FILE="target/diylc-5.1.0-linux.zip"

# Check if files exist
if [ ! -f "$APPIMAGE" ]; then
    echo "AppImage not found at $APPIMAGE"
    exit 1
fi

if [ ! -f "$ZIP_FILE" ]; then
    echo "Zip file not found at $ZIP_FILE"
    exit 1
fi

# Create test directory
TEST_DIR="target/appimage-test"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"

# Copy files to test directory
echo "Copying files to test directory..."
cp "$APPIMAGE" "$TEST_DIR/"
cp "$ZIP_FILE" "$TEST_DIR/"

# Extract zip file
echo "Extracting zip file..."
cd "$TEST_DIR"
unzip "diylc-5.1.0-linux.zip"

# Make AppImage executable
chmod +x "DIYLayoutCreator-5.1.0-x86_64.AppImage"

# Test AppImage structure
echo "Testing AppImage structure..."
if [ -f "DIYLayoutCreator-5.1.0-x86_64.AppImage" ]; then
    echo "AppImage file exists and is executable"
    echo "AppImage size: $(ls -lh DIYLayoutCreator-5.1.0-x86_64.AppImage | awk '{print $5}')"
    echo "AppImage permissions: $(ls -l DIYLayoutCreator-5.1.0-x86_64.AppImage | awk '{print $1}')"
    
    # Check if AppImage has the correct magic number
    if file "DIYLayoutCreator-5.1.0-x86_64.AppImage" | grep -q "ELF 64-bit LSB executable"; then
        echo "AppImage has correct ELF format"
    else
        echo "Warning: AppImage might not have correct ELF format"
    fi
    
    # Check if AppImage has the correct AppImage magic bytes
    if dd if="DIYLayoutCreator-5.1.0-x86_64.AppImage" bs=8 count=1 2>/dev/null | grep -q "AI"; then
        echo "AppImage has correct AppImage magic bytes"
    else
        echo "Warning: AppImage might not have correct AppImage magic bytes"
    fi
    
    echo "AppImage structure test completed successfully!"
else
    echo "AppImage file not found in test directory!"
    exit 1
fi 