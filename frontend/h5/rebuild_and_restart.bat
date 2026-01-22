@echo off
echo ==================================
echo Rebuilding H5 Frontend...
echo ==================================
echo.

cd /d "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\frontend\h5"

echo Stopping preview server...
taskkill /F /IM node.exe 2>nul
timeout /t 2 /nobreak >nul

echo Running npm build...
call npm run build

if %errorlevel% equ 0 (
    echo ==================================
    echo Build successful!
    echo ==================================
    echo.
    echo Starting preview server...
    call npm run preview
) else (
    echo ==================================
    echo Build failed!
    echo ==================================
    pause
)
