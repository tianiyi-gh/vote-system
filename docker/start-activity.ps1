# 启动activity-service测试
Write-Host "=== 启动DZVote Activity Service ===" -ForegroundColor Cyan

# 检查JAR文件
$jarPath = "..\backend\activity-service\target\activity-service-2.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "❌ 未找到JAR文件: $jarPath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到JAR文件" -ForegroundColor Green

# 设置环境变量
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/dzvote_new"
$env:SPRING_DATASOURCE_USERNAME = "dzvote"
$env:SPRING_DATASOURCE_PASSWORD = "dzvote123"
$env:SPRING_REDIS_HOST = "localhost"
$env:SPRING_REDIS_PORT = "6379"

Write-Host "📋 环境配置:" -ForegroundColor Yellow
Write-Host "   Profile: dev" -ForegroundColor White
Write-Host "   Database: dzvote_new" -ForegroundColor White
Write-Host "   Redis: localhost:6379" -ForegroundColor White

Write-Host "`n🚀 启动服务..." -ForegroundColor Blue
Write-Host "端口: 8081" -ForegroundColor White
Write-Host "健康检查: http://localhost:8081/actuator/health" -ForegroundColor White
Write-Host "API文档: http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "`n按Ctrl+C停止服务`n" -ForegroundColor Gray

# 启动Java进程
Set-Location "..\backend\activity-service\target"
java -jar activity-service-2.0.0.jar