@echo off
echo === MySQL 8 安装验证 ===
echo.

echo 检查服务状态...
netstat -an | findstr :3307 >nul
if %errorlevel% == 0 (
    echo ✓ MySQL 8 已运行在 3307 端口
) else (
    echo ✗ MySQL 8 未启动
)

netstat -an | findstr :8081 >nul
if %errorlevel% == 0 (
    echo ✓ Activity Service 已运行在 8081 端口 (连接MySQL 8)
) else (
    echo ✗ Activity Service 未启动
)

echo.
echo === 性能对比 ===
echo MySQL 5.7 (3306端口) - 基础版本
echo MySQL 8   (3307端口) - 高并发优化版本
echo.
echo === MySQL 8 性能优化配置 ===
echo - 最大连接数: 2000 (vs 5.7默认151)
echo - InnoDB线程池: 自动优化
echo - 缓冲池大小: 2GB
echo - 日志优化: 异步刷新
echo.
echo === 测试连接 ===
mysql -h localhost -P 3307 -u dzvote -pdzvote123 -e "SELECT 'MySQL 8连接成功!' as status, VERSION() as mysql_version, @@max_connections as max_connections;"

echo.
echo === 服务访问地址 ===
echo Activity Service: http://localhost:8081
echo 健康检查: http://localhost:8081/health
echo.
pause