@echo off
chcp 65001 >nul 2>&1
echo ================================================================
echo       DZVOTE 2.0 - Setup and Start (Java 17)
echo ================================================================
echo.

set "JAVA_HOME=D:\ide\Java\java17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [Step 1/4] Checking dependencies...
echo ================================================================
echo.

REM Check MySQL
echo Checking MySQL (port 3306)...
netstat -an | findstr "3306" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] MySQL is running
) else (
    echo   [X] MySQL is NOT running!
    echo.
    echo Please start MySQL first:
    echo   1. Start MySQL service
    echo   2. Or run: net start mysql
    echo.
    pause
    exit /b 1
)

echo.
echo Checking Redis (port 6379)...
netstat -an | findstr "6379" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Redis is running
) else (
    echo   [!] Redis is NOT running (optional)
    echo       Service will work without Redis
)

echo.
echo ================================================================
echo [Step 2/4] Database setup...
echo ================================================================
echo.

echo Do you need to initialize the database? (Y/N)
echo   Y - Create database and tables (first time setup)
echo   N - Skip (database already setup)
echo.
set /p DB_INIT="Your choice: "

if /i "%DB_INIT%"=="Y" (
    echo.
    echo Initializing database...
    echo Please enter MySQL root password when prompted.
    echo.
    
    REM Create database
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS dzvote_v2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    
    if errorlevel 1 (
        echo.
        echo [X] Database creation failed!
        pause
        exit /b 1
    )
    
    REM Import schema
    mysql -u root -p dzvote_v2 < sql\init_db.sql
    
    if errorlevel 1 (
        echo.
        echo [X] Schema import failed!
        pause
        exit /b 1
    )
    
    echo.
    echo   [OK] Database initialized successfully!
) else (
    echo.
    echo   [OK] Skipping database initialization
)

echo.
echo ================================================================
echo [Step 3/4] Testing database connection...
echo ================================================================
echo.

REM Test database connection
mysql -u root -p123456 -e "USE dzvote_v2; SELECT COUNT(*) FROM vote_activity;" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Database connection successful!
) else (
    echo   [!] Database connection failed!
    echo       Please check:
    echo       - MySQL is running
    echo       - Database 'dzvote_v2' exists
    echo       - Username/password in application.yml is correct
    echo.
    echo   Continuing anyway...
)

echo.
echo ================================================================
echo [Step 4/4] Starting service...
echo ================================================================
echo.

cd /d "%~dp0backend\activity-service"

echo Starting Activity Service (Standalone Mode)...
echo   Port: 8081
echo   Profile: standalone (Nacos disabled)
echo.
echo API Endpoints:
echo   - REST API: http://localhost:8081/api/activities
echo   - Swagger:  http://localhost:8081/swagger-ui.html
echo   - OpenAPI:  http://localhost:8081/v3/api-docs
echo.
echo Press Ctrl+C to stop the service
echo ================================================================
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=standalone

pause
