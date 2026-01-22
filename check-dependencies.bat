@echo off
echo ================================================================
echo       Checking Required Dependencies
echo ================================================================
echo.

echo [1/2] Checking MySQL...
netstat -an | findstr "3306" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] MySQL is running on port 3306
) else (
    echo   [X] MySQL is NOT running on port 3306
    echo       Please start MySQL first!
)

echo.
echo [2/2] Checking Redis...
netstat -an | findstr "6379" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Redis is running on port 6379
) else (
    echo   [X] Redis is NOT running on port 6379
    echo       Redis is optional, service may still work
)

echo.
echo ================================================================
echo.
pause
