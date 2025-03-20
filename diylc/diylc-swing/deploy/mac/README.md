# Setting up macOS Build Environment for DIYLC

This document describes how to set up a macOS machine for building and signing DIYLC application bundles.

## Prerequisites

1. **macOS System**
   - macOS 11 (Big Sur) or later
   - Xcode Command Line Tools installed
   ```bash
   xcode-select --install
   ```

2. **Java Development Kit**
   - Install JDK 17 (recommended to use SDKMAN!)
   ```bash
   # Install SDKMAN!
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"
   
   # Install JDK 17
   sdk install java 17.0.14-tem
   ```

3. **Maven**
   - Install Maven 3.9 or later
   ```bash
   sdk install maven
   ```

## Apple Developer Account Setup

1. **Apple Developer Account**
   - Enroll in the [Apple Developer Program](https://developer.apple.com/programs/)
   - You need a paid membership ($99/year) for distribution signing

2. **Developer Certificate**
   - Open Keychain Access
   - Go to Certificate Assistant → Request a Certificate from a Certificate Authority
   - Fill in your email and name
   - Save to disk
   - Go to [Apple Developer Certificates](https://developer.apple.com/account/resources/certificates/list)
   - Create a new Developer ID Application certificate
   - Download and double-click to install in Keychain

3. **Notarization Setup**
   - Create an app-specific password:
     1. Go to [Apple ID Account](https://appleid.apple.com/)
     2. Security → App-Specific Passwords → Generate Password
     3. Save this password securely

   - Create a keychain profile for notarization:
   ```bash
   xcrun notarytool store-credentials "AC_PASSWORD" \
     --apple-id "your.email@example.com" \
     --team-id "YOUR_TEAM_ID" \
     --password "app-specific-password"
   ```

## Environment Configuration

1. **Set Developer Identity**
   - Find your Developer ID in Keychain Access
   ```bash
   security find-identity -v -p codesigning
   ```
   - Set the environment variable (add to your ~/.zshrc):
   ```bash
   export APPLE_DEVELOPER_IDENTITY="Developer ID Application: Your Name (TEAM_ID)"
   ```

2. **Verify Setup**
   ```bash
   # Verify codesign is available
   codesign --version
   
   # Verify notarytool is available
   xcrun notarytool --version
   
   # Verify your identity is set
   echo $APPLE_DEVELOPER_IDENTITY
   ```

## Building DIYLC

1. **Clone the Repository**
   ```bash
   git clone https://github.com/bancika/diy-layout-creator.git
   cd diy-layout-creator/diylc
   ```

2. **Build for Intel Macs**
   ```bash
   mvn clean package -P mac-x86
   ```

3. **Build for Apple Silicon Macs**
   ```bash
   mvn clean package -P mac-arm
   ```

4. **Build Both Architectures**
   ```bash
   mvn clean package -P mac-x86,mac-arm
   ```

## Output Files

After successful builds, you'll find these files in the `diylc-swing/target` directory:

**Signed and Notarized Builds:**
- `diylc-<version>-osx-x86.zip` - Signed and notarized Intel build
- `diylc-<version>-osx-arm.zip` - Signed and notarized ARM build

**Unsigned Builds:**
- `diylc-<version>-osx-x86-unsigned.zip` - Unsigned Intel build
- `diylc-<version>-osx-arm-unsigned.zip` - Unsigned ARM build

The unsigned builds are created before the signing and notarization process and can be useful for testing or in environments where code signing is not required.

## Troubleshooting

1. **Signing Issues**
   - Verify your certificate is valid:
   ```bash
   security find-identity -v -p codesigning
   ```
   - Check certificate expiration in Keychain Access

2. **Notarization Issues**
   - Check notarization status:
   ```bash
   xcrun notarytool log <submission-id> --keychain-profile "AC_PASSWORD"
   ```
   - Verify entitlements are correct in `deploy/mac/entitlements.plist`

3. **Build Issues**
   - Clear Maven cache:
   ```bash
   rm -rf ~/.m2/repository/org/diylc
   ```
   - Verify JDK version:
   ```bash
   java -version
   ```

## Common Issues

1. **"The specified item could not be found in the keychain"**
   - Solution: Verify your Developer ID certificate is in the keychain
   - Check the exact name matches `APPLE_DEVELOPER_IDENTITY`

2. **"No account for team found"**
   - Solution: Verify your Apple Developer membership is active
   - Check team ID in notarytool credentials

3. **"The binary is not signed"**
   - Solution: Check that the signing process isn't being skipped
   - Verify all dependencies are properly signed

## Support

For additional help:
- Check the [Apple Code Signing Documentation](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution)
- File an issue on the DIYLC GitHub repository
- Contact the development team 