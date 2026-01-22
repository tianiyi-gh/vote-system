@echo off
chcp 65001 >nul
echo ========================================
echo    重启 Vote Service
echo ========================================
echo.

echo [1/3] 停止当前的 vote-service...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq vote-service*" 2>nul
timeout /t 2 >nul

echo [2/3] 启动 vote-service...
cd /d "%~dp0backend\vote-service"
start "vote-service" cmd /k "java -jar target\vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082"

echo.
echo [3/3] 等待服务启动...
timeout /t 5 >nul

echo.
echo ========================================
echo    重启完成！
echo ========================================
echo.
echo 访问地址：
echo   - 投票页面: http://localhost:8082/vote.html
echo   - 管理后台: http://localhost:8082/admin.html
echo.
pause
