@echo off
echo === 启动数据库服务 ===

cd /d "%~dp0"

echo 启动 MySQL 和 Redis...
docker-compose -f docker-compose-cn.yml up -d mysql redis

echo.
echo 等待数据库启动完成...
timeout /t 30

echo.
echo 检查数据库状态...
docker-compose -f docker-compose-cn.yml ps

echo.
echo 数据库连接信息:
echo MySQL: localhost:3306
echo - 用户名: root / 密码: root123456
echo - 数据库: dzvote_new
echo Redis: localhost:6379
echo.
pause