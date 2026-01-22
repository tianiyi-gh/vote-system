@echo off
chcp 65001 >nul
echo ========================================
echo DZVOTE 2.0 Build Script
echo ========================================
echo.

echo Building backend project...
cd /d "%~dp0backend"
call mvn clean install -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo Build SUCCESS!
    echo ========================================
    echo.
    echo Next step: Run start.bat to launch services
) else (
    echo.
    echo ========================================
    echo Build FAILED!
    echo ========================================
    echo Please check the error messages above.
)

echo.
pause
