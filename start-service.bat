@echo off
echo ================================================================
echo       Starting Activity Service (Java 17 - Standalone Mode)
echo ================================================================
echo.

set "JAVA_HOME=D:\ide\Java\java17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0backend\activity-service"

echo Starting Activity Service on port 8081...
echo Mode: Standalone (Nacos disabled)
echo.
echo API Endpoint: http://localhost:8081/api/activities
echo Swagger UI:   http://localhost:8081/swagger-ui.html
echo.
echo Press Ctrl+C to stop the service
echo ================================================================
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=standalone
