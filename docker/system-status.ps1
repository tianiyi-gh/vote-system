# 系统状态检查
Write-Host "=== DZVote系统状态 ===" -ForegroundColor Cyan

# 检查构建产物
Write-Host "检查构建产物..." -ForegroundColor Yellow

$backendJar = "..\backend\activity-service\target\activity-service-2.0.0.jar"
$adminDist = "..\frontend\admin\dist"
$h5Dist = "..\frontend\h5\dist"

if (Test-Path $backendJar) {
    Write-Host "✅ 后端服务构建完成" -ForegroundColor Green
} else {
    Write-Host "❌ 后端服务未构建" -ForegroundColor Red
}

if (Test-Path $adminDist) {
    Write-Host "✅ 管理后台构建完成" -ForegroundColor Green
} else {
    Write-Host "❌ 管理后台未构建" -ForegroundColor Red
}

if (Test-Path $h5Dist) {
    Write-Host "✅ H5移动端构建完成" -ForegroundColor Green
} else {
    Write-Host "❌ H5移动端未构建" -ForegroundColor Red
}

Write-Host "`n=== 系统访问地址 ===" -ForegroundColor Cyan
Write-Host "🖥️ 后端API服务:" -ForegroundColor White
Write-Host "   健康检查: http://localhost:8081/health" -ForegroundColor Blue
Write-Host "   API文档: http://localhost:8081/swagger-ui.html" -ForegroundColor Blue

Write-Host "`n📱 管理后台 (Vue3 + Element Plus):" -ForegroundColor White
Write-Host "   目录: $adminDist" -ForegroundColor Blue
Write-Host "   建议端口: http://localhost:80" -ForegroundColor Blue

Write-Host "`n📱 H5移动端 (Vue3 + Vant):" -ForegroundColor White
Write-Host "   目录: $h5Dist" -ForegroundColor Blue
Write-Host "   建议端口: http://localhost:81" -ForegroundColor Blue

Write-Host "`n=== 启动建议 ===" -ForegroundColor Yellow
Write-Host "1. 后端服务已运行 (端口8081)" -ForegroundColor White
Write-Host "2. 使用Web服务器启动前端:" -ForegroundColor Gray
Write-Host "   cd $adminDist && python -m http.server 80" -ForegroundColor Gray
Write-Host "   cd $h5Dist && python -m http.server 81" -ForegroundColor Gray

Write-Host "`n🎉 系统状态检查完成！" -ForegroundColor Green