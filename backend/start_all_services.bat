@echo off
chcp 65001 >nul

echo ========================================
echo 启动 DZVote 所有服务
echo ========================================

echo.
echo [1/3] 启动 Redis...
docker ps | findstr "dzvote-redis" >nul
if errorlevel 1 (
    echo Redis 容器不存在，正在创建...
    docker run -d --name dzvote-redis -p 6379:6379 redis:7-alpine redis-server --appendonly yes
) else (
    docker start dzvote-redis >nul 2>&1
)
echo [1/3] Redis 已启动

echo.
echo [2/3] 启动 vote-service...
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
start "DZVote Vote Service" /min javaw -Xms512m -Xmx1024m -jar target/vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082
echo [2/3] vote-service 已启动

echo.
echo [3/3] 启动 user-service...
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\user-service
start "DZVote User Service" /min javaw -Xms512m -Xmx1024m -jar target/user-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8084
echo [3/3] user-service 已启动

echo.
echo ========================================
echo 所有服务已启动！
echo ========================================
echo.
echo 服务地址:
echo   vote-service:  http://localhost:8082
echo   user-service:  http://localhost:8084
echo   Redis:        localhost:6379
echo   MySQL:        localhost:3307
echo.
echo 测试命令:
echo   curl http://localhost:8082/api/activities
echo   curl http://localhost:8082/api/activities/service/ACT000006/candidates/20
echo   docker exec dzvote-redis redis-cli ping
echo.
echo 查看服务状态:
echo   tasklist ^| findstr java
echo   docker ps
echo   netstat -ano ^| findstr "8082 8084"
echo.
echo 停止所有服务:
echo   taskkill /F /IM java.exe
echo   docker stop dzvote-redis
echo.
pause
