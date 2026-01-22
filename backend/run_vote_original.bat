@echo off
chcp 65001 >nul
echo Starting vote-service...
cd /d "%~dp0vote-service\target"
java -jar vote-service-2.0.0.jar.original --spring.profiles.active=standalone
pause
