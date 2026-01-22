@echo off
echo Restarting vote-service...

REM 停止可能存在的旧进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082') do (
    taskkill /F /PID %%a 2>nul
)

echo Starting vote-service...
cd /d %~dp0

REM 检查是否有jar文件
if exist target\vote-service-2.0.0.jar (
    echo Running from JAR file...
    java -jar target\vote-service-2.0.0.jar
) else (
    echo JAR file not found. Please run mvn clean package first.
    pause
)
