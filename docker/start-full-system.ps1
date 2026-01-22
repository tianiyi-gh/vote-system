# 启动完整DZVote系统
Write-Host "=== 启动DZVote完整系统 ===" -ForegroundColor Cyan

Write-Host "检查构建产物..." -ForegroundColor Yellow

# 检查后端JAR
$backendJar = "..\backend\activity-service\target\activity-service-2.0.0.jar"
if (Test-Path $backendJar) {
    Write-Host "✅ 后端服务构建完成" -ForegroundColor Green
} else {
    Write-Host "❌ 后端服务未构建" -ForegroundColor Red
    exit 1
}

# 检查前端构建
$adminDist = "..\frontend\admin\dist"
$h5Dist = "..\frontend\h5\dist"

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
Write-Host "   请使用Web服务器启动: $adminDist" -ForegroundColor Blue
Write-Host "   建议端口: http://localhost:80" -ForegroundColor Blue

Write-Host "`n📱 H5移动端 (Vue3 + Vant):" -ForegroundColor White
Write-Host "   请使用Web服务器启动: $h5Dist" -ForegroundColor Blue
Write-Host "   建议端口: http://localhost:81" -ForegroundColor Blue

Write-Host "`n=== 快速启动Web服务器 ===" -ForegroundColor Yellow
Write-Host "如果您有Python，可以使用:" -ForegroundColor Gray
Write-Host "cd $adminDist && python -m http.server 80" -ForegroundColor Gray
Write-Host "cd $h5Dist && python -m http.server 81" -ForegroundColor Gray

Write-Host "`n=== Docker启动选项 ===" -ForegroundColor Yellow
Write-Host "修复Docker网络问题后可使用:" -ForegroundColor Gray
Write-Host "docker-compose up -d --build" -ForegroundColor Gray

Write-Host "`n系统状态检查完成！🎉" -ForegroundColor Green