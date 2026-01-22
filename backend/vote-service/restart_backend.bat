@echo off
echo Stopping existing Java processes...
taskkill /F /IM java.exe 2>nul
timeout /t 3 /nobreak >nul

echo Starting Vote Service...
cd /d "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service"
start "Vote Service" java -jar target\vote-service-2.0.0.jar --spring.profiles.active=standalone

echo Vote Service is starting...
echo Service will be available at: http://localhost:8082
echo.
echo Check logs for startup status.
