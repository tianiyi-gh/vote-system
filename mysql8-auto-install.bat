@echo off
echo === MySQL 8 自动安装脚本 ===
echo.

set MYSQL8_DIR=D:\mysql80
set MYSQL8_PORT=3307
set MYSQL8_PASSWORD=mysql8123

echo 1. 检查管理员权限...
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo 请以管理员身份运行此脚本！
    pause
    exit /b 1
)

echo 2. 创建MySQL 8目录...
if not exist "%MYSQL8_DIR%" mkdir "%MYSQL8_DIR%"
if not exist "%MYSQL8_DIR%\data" mkdir "%MYSQL8_DIR%\data"

echo 3. 创建配置文件...
(
echo [mysqld]
echo port=%MYSQL8_PORT%
echo basedir=%MYSQL8_DIR%
echo datadir=%MYSQL8_DIR%\data
echo socket=mysql8.sock
echo.
echo # 并发性能优化
echo max_connections=2000
echo innodb_thread_concurrency=0
echo innodb_read_io_threads=8
echo innodb_write_io_threads=8
echo innodb_buffer_pool_size=2G
echo innodb_log_file_size=512M
echo innodb_flush_log_at_trx_commit=2
echo innodb_flush_method=O_DIRECT
echo.
echo # 字符集配置
echo character-set-server=utf8mb4
echo collation-server=utf8mb4_unicode_ci
echo.
echo [client]
echo port=%MYSQL8_PORT%
echo default-character-set=utf8mb4
echo.
echo [mysql]
echo default-character-set=utf8mb4
) > "%MYSQL8_DIR%\my.cnf"

echo 配置文件已创建: %MYSQL8_DIR%\my.cnf
echo.

echo 4. 请手动下载MySQL 8 ZIP包并解压到 %MYSQL8_DIR%
echo 下载地址: https://dev.mysql.com/downloads/mysql/8.0.html
echo.
echo 5. 下载完成后，请运行以下命令：
echo    cd %MYSQL8_DIR%\bin
echo    mysqld --initialize --console
echo    mysqld --install MySQL80 --defaults-file=%MYSQL8_DIR%\my.cnf
echo    net start MySQL80
echo.
echo 6. 然后运行配置脚本: mysql8-setup.bat
echo.

pause