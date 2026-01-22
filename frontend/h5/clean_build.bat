@echo off
echo ==================================
echo Clean and Rebuild H5 Frontend
echo ==================================
echo.

cd /d "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\frontend\h5"

echo Cleaning dist folder...
if exist dist (
    rmdir /s /q dist
    echo Dist folder cleaned.
)

echo Running npm build...
call npm run build

if %errorlevel% equ 0 (
    echo ==================================
    echo Build successful!
    echo ==================================
    echo.
    echo Please restart preview server:
    echo 1. Press Ctrl+C to stop current preview
    echo 2. Run: npm run preview
) else (
    echo ==================================
    echo Build failed!
    echo ==================================
)

pause
