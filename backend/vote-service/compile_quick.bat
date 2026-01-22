@echo off
chcp 65001 >nul
echo Rebuilding vote-service...
cd /d "%~dp0"
D:/ide/maven3.9/bin/mvn.cmd -f pom.xml clean package -DskipTests
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)
echo Build successful!
pause
