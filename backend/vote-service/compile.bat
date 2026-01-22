@echo off
echo ==================================
echo Compiling Vote Service
echo ==================================
echo.

cd /d "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service"

echo Stopping Java processes...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo Compiling with Maven...
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo ==================================
    echo Compilation successful!
    echo ==================================
    echo.
    echo To start the service, run:
    echo   D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\restart_backend.bat
) else (
    echo ==================================
    echo Compilation failed!
    echo ==================================
    pause
)
