# 最小化activity-service测试启动
Write-Host "=== 启动最小化Activity Service ===" -ForegroundColor Cyan

# 检查JAR文件
$jarPath = "..\backend\activity-service\target\activity-service-2.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "❌ 未找到JAR文件: $jarPath" -ForegroundColor Red
    Write-Host "正在构建..." -ForegroundColor Yellow
    
    Set-Location "..\backend\activity-service"
    mvn clean package -DskipTests
    Set-Location "..\..\docker"
    
    if (-not (Test-Path $jarPath)) {
        Write-Host "❌ 构建失败" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ 找到JAR文件" -ForegroundColor Green

Write-Host "`n🚀 启动最小化服务..." -ForegroundColor Blue
Write-Host "端口: 8081" -ForegroundColor White
Write-Host "健康检查: http://localhost:8081/test/health" -ForegroundColor White
Write-Host "API文档: http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "`n按Ctrl+C停止服务`n" -ForegroundColor Gray

# 启动Java进程 - 使用test profile
Set-Location "..\backend\activity-service\target"
java -jar activity-service-2.0.0.jar --spring.profiles.active=test