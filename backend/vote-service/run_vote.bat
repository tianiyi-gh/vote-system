@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo Starting Vote Service...
echo.

java -jar target/vote-service-2.0.0.jar --spring.profiles.active=standalone

pause
