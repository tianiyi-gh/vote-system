@echo off
chcp 65001 >nul
echo ========================================
echo 执行投票限制字段升级脚本
echo ========================================
echo.

set MYSQL_HOST=localhost
set MYSQL_PORT=3307
set MYSQL_USER=root
set MYSQL_PASSWORD=mysql8123
set MYSQL_DATABASE=dzvote_v2

echo 数据库信息:
echo   主机: %MYSQL_HOST%:%MYSQL_PORT%
echo   用户: %MYSQL_USER%
echo   数据库: %MYSQL_DATABASE%
echo.

set /p confirm=确认执行? (y/n):
if /i not "%confirm%"=="y" (
    echo 操作已取消
    pause
    exit /b
)

echo.
echo 正在执行 SQL 脚本...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% < add_vote_limit_fields.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo ✓ 升级成功！
    echo ========================================
    echo.
    echo 新增字段:
    echo   - daily_candidate_limit: 每天最多对多少名候选人投票
    echo   - candidate_daily_limit: 每个候选人每天限投多少次
    echo.
    echo 默认值:
    echo   - daily_candidate_limit: 5
    echo   - candidate_daily_limit: 1
    echo.
) else (
    echo.
    echo ========================================
    echo ✗ 升级失败！
    echo ========================================
    echo.
    echo 请检查:
    echo 1. MySQL 服务是否启动
    echo 2. 数据库连接信息是否正确
    echo 3. SQL 脚本是否存在
    echo.
)

pause
