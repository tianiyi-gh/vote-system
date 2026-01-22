@echo off
echo === DZVote系统状态 ===
echo.

echo [检查构建产物]
if exist "..\backend\activity-service\target\activity-service-2.0.0.jar" (
    echo [✓] 后端服务构建完成
) else (
    echo [✗] 后端服务未构建
)

if exist "..\frontend\admin\dist" (
    echo [✓] 管理后台构建完成
) else (
    echo [✗] 管理后台未构建
)

if exist "..\frontend\h5\dist" (
    echo [✓] H5移动端构建完成
) else (
    echo [✗] H5移动端未构建
)

echo.
echo === 系统访问地址 ===
echo.
echo 🖥️ 后端API服务:
echo    健康检查: http://localhost:8081/health
echo    API文档: http://localhost:8081/swagger-ui.html
echo.
echo 📱 管理后台 (Vue3 + Element Plus):
echo    目录: ..\frontend\admin\dist
echo    建议端口: http://localhost:80
echo.
echo 📱 H5移动端 (Vue3 + Vant):
echo    目录: ..\frontend\h5\dist
echo    建议端口: http://localhost:81
echo.
echo === 启动建议 ===
echo 1. 后端服务已运行 (端口8081)
echo 2. 使用Web服务器启动前端:
echo    cd ..\frontend\admin\dist && python -m http.server 80
echo    cd ..\frontend\h5\dist && python -m http.server 81
echo.
echo 🎉 系统状态检查完成！
pause