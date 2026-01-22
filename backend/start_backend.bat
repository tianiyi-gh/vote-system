@echo off
chcp 65001 >nul

echo ========================================
echo 启动 DZVote 后端服务
echo ========================================

echo.
echo [1/2] 启动 vote-service...
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
start "DZVote Vote Service" /min javaw -Xms512m -Xmx1024m -jar target/vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082
echo [1/2] vote-service 已启动

echo.
echo [2/2] 启动 user-service...
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\user-service
start "DZVote User Service" /min javaw -Xms512m -Xmx1024m -jar target/user-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8084
echo [2/2] user-service 已启动

echo.
echo ========================================
echo 所有服务已启动！
echo vote-service:  http://localhost:8082
echo user-service: http://localhost:8084
echo ========================================
echo.
echo 提示：查看 vote-service 启动情况:
echo     netstat -ano | findstr "8082"
echo     tasklist | findstr java
echo.
pause
