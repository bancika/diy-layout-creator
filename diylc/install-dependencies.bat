@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo DIYLC Maven Local Dependency Installer for Windows
echo ===================================================
echo.
echo This script will install all JAR files from the lib directories
echo to your local Maven repository.
echo.

REM Define the group ID for all local dependencies
set GROUP_ID=org.diylc.local

REM List of module lib directories to process
set MODULES=diylc-swing

echo Processing dependencies...
echo.

for %%m in (%MODULES%) do (
    echo Module: %%m
    echo -----------------------
    
    if exist "%%m\lib" (
        for %%j in ("%%m\lib\*.jar") do (
            REM Extract filename without extension
            set "JAR_FILE=%%j"
            set "ARTIFACT_ID=%%~nj"
            
            echo Installing !ARTIFACT_ID!.jar
            
            REM Install the JAR to the local Maven repository
            call mvn install:install-file ^
                -Dfile="!JAR_FILE!" ^
                -DgroupId=%GROUP_ID% ^
                -DartifactId=!ARTIFACT_ID! ^
                -Dversion=1.0 ^
                -Dpackaging=jar ^
                -DgeneratePom=true ^
                -DcreateChecksum=true
                
            echo.
        )
    ) else (
        echo No lib directory found for %%m
        echo.
    )
)

echo ===================================================
echo All dependencies have been installed!
echo.
echo You can now reference these dependencies in your POM files as:
echo.
echo ^<dependency^>
echo     ^<groupId^>%GROUP_ID%^</groupId^>
echo     ^<artifactId^>ARTIFACT_NAME^</artifactId^>
echo     ^<version^>1.0^</version^>
echo ^</dependency^>
echo ===================================================

pause
