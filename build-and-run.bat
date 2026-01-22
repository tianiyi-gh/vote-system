@echo off
echo ================================================================
echo       DZVOTE 2.0 - Build and Run Script (Java 17)
echo ================================================================
echo.

echo [Step 1/3] Setting Java 17 environment...
set "JAVA_HOME=D:\ide\Java\java17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo   JAVA_HOME = %JAVA_HOME%

echo.
echo Verifying Java version...
java -version
echo.
echo Verifying Maven uses correct Java...
mvn -version
echo.

echo ================================================================
echo [Step 2/3] Building project...
echo ================================================================
cd /d "%~dp0backend"

echo.
echo Cleaning old build files...
call mvn clean

echo.
echo Building all modules (this may take 5-10 minutes)...
call mvn install -DskipTests

if errorlevel 1 (
    echo.
    echo ================================================================
    echo   BUILD FAILED!
    echo ================================================================
    echo.
    pause
    exit /b 1
)

echo.
echo ================================================================
echo   BUILD SUCCESS!
echo ================================================================
echo.

echo ================================================================
echo [3/3] Starting Activity Service...
echo ================================================================
cd activity-service
echo.
echo Starting on port 8081...
echo API: http://localhost:8081/api/activities
echo Swagger: http://localhost:8081/swagger-ui.html
echo.

call mvn spring-boot:run

pause
