@echo off
echo === DZVote 系统启动脚本 ===
echo.

echo 检查服务状态...
netstat -an | findstr :8081 >nul
if %errorlevel% == 0 (
    echo ✓ Activity Service 已运行在 8081 端口
) else (
    echo ✗ Activity Service 未启动
)

netstat -an | findstr :6379 >nul
if %errorlevel% == 0 (
    echo ✓ Redis 已运行在 6379 端口
) else (
    echo ✗ Redis 未启动
)

netstat -an | findstr :3306 >nul
if %errorlevel% == 0 (
    echo ✓ MySQL 已运行在 3306 端口
) else (
    echo ✗ MySQL 未启动
)

echo.
echo === 服务访问地址 ===
echo 后端服务:
echo - Activity Service: http://localhost:8081
echo - 健康检查: http://localhost:8081/health
echo - API文档: http://localhost:8081/swagger-ui.html
echo.
echo 前端应用:
echo - 管理后台: file:///d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/frontend/admin/dist/index.html
echo - H5投票端: file:///d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/frontend/h5/dist/index.html
echo.
echo === 数据库信息 ===
echo MySQL: localhost:3306
echo - 用户名: root / 密码: 123456
echo - 数据库: dzvote_v2
echo Redis: localhost:6379
echo.

pause