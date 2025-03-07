# 🚀 Setting up WSL and AppImage Builder

This script sets up **WSL (Windows Subsystem for Linux)** and installs the necessary dependencies for building AppImages.

---

## 📜 Installation Script

```batch
@echo off
echo ========================================
echo  🚀 Setting up WSL and AppImage Builder
echo ========================================
echo.

:: Check if WSL is installed
wsl --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 🔧 Installing WSL...
    wsl --install -d Ubuntu22.04
    echo ✅ WSL installed! Please restart your computer and re-run this script.
    exit /b
)

echo 🔧 Ensuring Ubuntu is set as default WSL distribution...
wsl --set-default Ubuntu

:: Check if WSL is running
wsl echo "WSL is running..."
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: WSL is not running. Please restart your system and try again.
    exit /b
)

:: Update and install dependencies inside WSL
echo 🔧 Installing dependencies inside WSL...
wsl sudo apt update && sudo apt install -y ^
    wget ^
    fuse ^
    libfuse2 ^
    ca-certificates ^
    xz-utils ^
    file ^
    appstream ^
    gpg ^
    libc6 ^
    binfmt-support ^
    tree

wsl sudo apt update && sudo apt install -y libfuse2

echo ✅ Dependencies installed!

:: Download AppImageTool inside WSL
echo 🔧 Downloading AppImageTool...
wsl bash -c "wget -O ~/appimagetool https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage && chmod +x ~/appimagetool"

:: Verify AppImageTool installation
echo 🔧 Verifying AppImageTool installation...
wsl bash -c "~/appimagetool --version"

if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: AppImageTool failed to execute.
    echo Please check if WSL is running properly and try again.
    exit /b
)

echo ✅ WSL is now ready for ANT to build AppImages!
echo.

:: Optional: Open WSL shell for user confirmation
echo 🔧 Opening WSL shell...
wsl

exit /b
