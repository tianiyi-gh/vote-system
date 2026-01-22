@echo off
cd /d "%~dp0"

echo ========================================
echo User Service 启动脚本
echo ========================================
echo.
echo 方式 1: 使用已存在的 jar 文件（推荐）
echo 方式 2: 从编译的类运行
echo.
set /p choice=请选择启动方式 (1 或 2):

if "%choice%"=="1" goto run_jar
if "%choice%"=="2" goto run_classes
echo 无效的选择，默认使用 jar 方式
goto run_jar

:run_jar
echo.
echo 正在使用 jar 文件启动...
java -jar target\user-service-2.0.0.jar --spring.profiles.active=standalone
goto end

:run_classes
echo.
echo 正在从编译的类启动...
REM 使用 Spring Boot 的 DevTools 方式启动
set SPRING_APPLICATION_JSON={"spring":{"devtools":{"restart":{"enabled":false}}}}
java -cp "target/classes" -Dloader.path="" -Dloader.main=com.dzvote.user.UserApplication org.springframework.boot.loader.launch.JarLauncher --spring.profiles.active=standalone
goto end

:end
echo.
pause
