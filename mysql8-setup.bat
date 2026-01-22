@echo off
echo === MySQL 8 配置和数据库设置 ===
echo.

set MYSQL8_PORT=3307
set MYSQL8_PASSWORD=mysql8123

echo 1. 等待MySQL 8启动...
timeout /t 10

echo 2. 创建dzvote数据库和用户...
mysql -h localhost -P %MYSQL8_PORT% -u root -p --execute="
CREATE DATABASE IF NOT EXISTS dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'dzvote'@'localhost' IDENTIFIED BY 'dzvote123';
GRANT ALL PRIVILEGES ON dzvote_v2.* TO 'dzvote'@'localhost';
FLUSH PRIVILEGES;
SELECT 'MySQL 8 配置完成' as status;
"

echo.
echo 3. 更新应用配置文件...

echo 更新Activity Service配置...
(
echo server:
echo   port: 8081
echo.
echo spring:
echo   application:
echo     name: activity-service
echo.
echo   datasource:
echo     driver-class-name: com.mysql.cj.jdbc.Driver
echo     url: jdbc:mysql://localhost:%MYSQL8_PORT%/dzvote_v2?useUnicode=true^&characterEncoding=utf8mb4^&serverTimezone=Asia/Shanghai^&useSSL=false
echo     username: dzvote
echo     password: dzvote123
echo     hikari:
echo       minimum-idle: 5
echo       maximum-pool-size: 20
echo       connection-timeout: 30000
echo       idle-timeout: 600000
echo       max-lifetime: 1800000
echo.
echo   data:
echo     redis:
echo       host: localhost
echo       port: 6379
echo       database: 1
) > "d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/src/main/resources/application-mysql8.yml"

echo.
echo 4. 数据库连接信息:
echo    MySQL 8: localhost:%MYSQL8_PORT%
echo    用户名: dzvote
echo    密码: dzvote123
echo    数据库: dzvote_v2
echo.

echo 5. 测试连接...
mysql -h localhost -P %MYSQL8_PORT% -u dzvote -pdzvote123 -e "SELECT '连接成功!' as status, VERSION() as mysql_version;"

echo.
echo === 配置完成 ===
echo 下一步:
echo 1. 重启Activity Service使用MySQL 8配置
echo 2. 运行: java -jar target/activity-service-2.0.0.jar --spring.profiles.active=mysql8
echo.

pause