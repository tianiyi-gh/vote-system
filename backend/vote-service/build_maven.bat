@echo off
cd /d "%~dp0"
echo Building vote-service with Maven...

echo Cleaning...
D:\ide\maven3.9\bin\mvn.cmd clean

echo Compiling and packaging...
D:\ide\maven3.9\bin\mvn.cmd package -DskipTests

if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build complete!
pause
