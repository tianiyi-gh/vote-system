@echo off
cd /d "%~dp0"

echo 重新打包 user-service...
cd user-service
call D:/ide/maven3.9/bin/mvn.cmd clean package -DskipTests
if errorlevel 1 (
    echo user-service 打包失败!
    pause
    exit /b 1
)
echo user-service 打包成功!
echo.

cd ..

echo 重新打包 vote-service...
cd vote-service
call D:/ide/maven3.9/bin/mvn.cmd clean package -DskipTests
if errorlevel 1 (
    echo vote-service 打包失败!
    pause
    exit /b 1
)
echo vote-service 打包成功!
echo.

echo ========================================
echo 所有服务打包完成!
echo ========================================
pause
