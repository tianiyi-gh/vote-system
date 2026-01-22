@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo 启动 DZVote 后端服务
echo ========================================

echo.
echo [1/2] 启动 vote-service...
start "DZVote Vote Service" /D "d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service" ^
    javaw -Xms512m -Xmx1024m ^
    -jar target/vote-service-2.0.0.jar ^
    --spring.profiles.active=standalone ^
    --server.port=8082

echo [1/2] vote-service 已在后台启动

echo.
timeout /t 3 >nul

echo [2/2] 启动 user-service...
start "DZVote User Service" /D "d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service" ^
    javaw -Xms512m -Xmx1024m ^
    -jar target/user-service-2.0.0.jar ^
    --spring.profiles.active=standalone ^
    --server.port=8083

echo [2/2] user-service 已在后台启动

echo.
echo ========================================
echo 所有服务已启动！
echo vote-service:  http://localhost:8082
echo user-service: http://localhost:8083
echo ========================================
echo.
echo 提示：关闭此窗口不会停止服务
echo 要停止服务，请在任务管理器中结束 java.exe 进程
echo.
pause
