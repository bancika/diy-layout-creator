@echo off
setlocal

rem Get the directory where the batch file is located
set "DIR=%~dp0"
set "CLASS_PATH=%DIR%diylc.jar"

rem Run DIYLC with appropriate memory and Java module settings
java ^
    -Xms512m ^
    -Xmx4096m ^
    -Dorg.diylc.scriptRun=true ^
    -Dfile.encoding=UTF-8 ^
    -cp "%CLASS_PATH%" ^
    --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED ^
    --add-exports java.desktop/com.apple.eio=ALL-UNNAMED ^
    --add-opens java.base/java.util=ALL-UNNAMED ^
    --add-opens java.base/java.lang=ALL-UNNAMED ^
    --add-opens java.base/java.text=ALL-UNNAMED ^
    --add-opens java.desktop/java.awt=ALL-UNNAMED ^
    --add-opens java.desktop/java.awt.font=ALL-UNNAMED ^
    --add-opens java.desktop/java.awt.geom=ALL-UNNAMED ^
    org.diylc.DIYLCStarter %*
