@echo off
chcp 65001 >nul
echo ========================================
echo DZVOTE 2.0 Startup Script
echo ========================================
echo.

echo [Step 1/3] Building backend...
cd /d "%~dp0backend"
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed!
    pause
    exit /b 1
)

echo.
echo [Step 2/3] Starting Activity Service...
start "DZVOTE-Backend" cmd /k "cd /d %~dp0backend\activity-service && mvn spring-boot:run"

echo.
echo [Step 3/3] Waiting 10 seconds for backend to start...
timeout /t 10 /nobreak

echo.
echo [Step 3/3] Starting Frontend...
start "DZVOTE-Frontend" cmd /k "cd /d %~dp0frontend\admin && npm run dev"

echo.
echo ========================================
echo Services are starting...
echo ========================================
echo.
echo Backend API: http://localhost:8081/swagger-ui.html
echo Frontend:    http://localhost:3000
echo.
echo Press any key to exit this window...
pause >nul
