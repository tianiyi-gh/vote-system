@echo off
echo Rebuilding H5 frontend...
cd /d "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\frontend\h5"
call npm run build

if %errorlevel% equ 0 (
    echo ==================================
    echo Build completed!
    echo ==================================
    echo.
    echo Please restart the preview server manually:
    echo 1. Press Ctrl+C to stop current preview
    echo 2. Run: npm run preview
    echo.
    echo The site will be available at: http://10.19.95.128:3002
) else (
    echo ==================================
    echo Build failed!
    echo ==================================
)

pause
