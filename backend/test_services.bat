@echo off
chcp 65001 >nul
echo Testing user-service...
echo.
curl http://localhost:8084/api/auth/health
echo.
echo.
echo Testing vote-service...
echo.
curl http://localhost:8085/api/votes/health
echo.
pause
