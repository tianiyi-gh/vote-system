@echo off
echo ==================================
echo Building H5 Frontend...
echo ==================================
echo.

cd /d "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\frontend\h5"

echo Running npm build...
call npm run build

if %errorlevel% equ 0 (
    echo ==================================
    echo Build successful!
    echo ==================================
    echo.
    echo Starting preview server on port 3002...
    call npm run preview
) else (
    echo ==================================
    echo Build failed!
    echo ==================================
)

pause
