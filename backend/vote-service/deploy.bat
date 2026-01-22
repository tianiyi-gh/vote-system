@echo off
REM 投票系统部署脚本 (Windows 版本)
REM 用途：将本地构建的文件上传到服务器并重启服务

REM ==================== 配置区域 ====================

REM 服务器信息
set SERVER_USER=root
set SERVER_IP=
set SERVER_PORT=22

REM 部署路径
set BACKEND_DIR=/opt/vote-service
set FRONTEND_DIR=/opt/h5

REM 本地文件路径
set BACKEND_JAR=target\vote-service-2.0.0.jar
set FRONTEND_DIST=新架构\frontend\h5\dist

REM ==========================================================

if "%SERVER_IP%"=="" (
    echo 错误：请先设置 SERVER_IP 变量
    echo 编辑此文件，将 set SERVER_IP= 改为你的服务器IP
    pause
    exit /b 1
)

echo ========================================
echo    投票系统部署脚本 (Windows)
echo ========================================
echo 服务器: %SERVER_USER%@%SERVER_IP%
echo 后端目录: %BACKEND_DIR%
echo 前端目录: %FRONTEND_DIR%
echo ========================================

REM 检查本地文件
echo.
echo 检查本地文件...

if not exist "%BACKEND_JAR%" (
    echo 错误：后端 jar 文件不存在: %BACKEND_JAR%
    echo 请先运行: mvn clean package -DskipTests
    pause
    exit /b 1
)

if not exist "%FRONTEND_DIST%" (
    echo 错误：前端构建目录不存在: %FRONTEND_DIST%
    echo 请先运行: cd 新架构\frontend\h5 ^&^& npm run build
    pause
    exit /b 1
)

echo [OK] 本地文件检查通过

REM 询问是否重新构建前端
echo.
set /p rebuild_frontend=是否重新构建前端？(y/n):
if /i "%rebuild_frontend%"=="y" (
    echo 构建前端...
    cd 新架构\frontend\h5
    call npm install
    if errorlevel 1 (
        echo 错误：npm install 失败
        pause
        exit /b 1
    )
    call npm run build
    if errorlevel 1 (
        echo 错误：前端构建失败
        pause
        exit /b 1
    )
    cd ..\..\..
    echo [OK] 前端构建完成
)

REM 1. 部署后端
echo.
echo ========================================
echo 步骤 1/3: 部署后端
echo ========================================

echo 上传后端 jar 文件...
pscp -P %SERVER_PORT% "%BACKEND_JAR%" %SERVER_USER%@%SERVER_IP%:%BACKEND_DIR%/
if errorlevel 1 (
    echo 错误：上传后端失败
    echo 请确认已安装 pscp 工具 (PuTTY)
    pause
    exit /b 1
)
echo [OK] 后端上传完成

REM 2. 部署前端
echo.
echo ========================================
echo 步骤 2/3: 部署前端
echo ========================================

echo 上传前端文件...
pscp -P %SERVER_PORT% -r "%FRONTEND_DIST%\*" %SERVER_USER%@%SERVER_IP%:%FRONTEND_DIR%/dist/
if errorlevel 1 (
    echo 错误：上传前端失败
    pause
    exit /b 1
)
echo [OK] 前端上传完成

REM 3. 重启服务
echo.
echo ========================================
echo 步骤 3/3: 重启服务
echo ========================================

echo 在服务器上执行重启命令...
plink -P %SERVER_PORT% %SERVER_USER%@%SERVER_IP% -m "systemctl stop vote-service; mv %BACKEND_DIR%/vote-service-2.0.0.jar %BACKEND_DIR%/vote-service-2.0.0.jar.bak 2^>nul; mv %BACKEND_DIR%/vote-service-2.0.0.jar.bak %BACKEND_DIR%/vote-service-2.0.0.jar 2^>nul; systemctl start vote-service; systemctl restart nginx; echo 部署完成"

if errorlevel 1 (
    echo 错误：重启服务失败
    echo 请确认已安装 plink 工具 (PuTTY)
    pause
    exit /b 1
)

echo.
echo ========================================
echo    部署完成！
echo ========================================
echo 访问地址: http://%SERVER_IP%
echo 后端 API: http://%SERVER_IP%/api
echo.
echo 查看日志:
echo   后端: journalctl -u vote-service -f
echo   Nginx: tail -f /var/log/nginx/error.log
echo ========================================

pause
