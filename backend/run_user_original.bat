@echo off
chcp 65001 >nul
echo Starting user-service...
cd /d "%~dp0user-service\target"
java -jar user-service-2.0.0.jar.original --spring.profiles.active=standalone
pause
