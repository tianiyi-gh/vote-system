# 简化启动脚本 - 仅测试已构建的activity-service
Write-Host "启动DZVote测试环境..." -ForegroundColor Green

# 检查已构建的JAR文件
$activityJar = "..\backend\activity-service\target\activity-service-2.0.0.jar"
if (Test-Path $activityJar) {
    Write-Host "✓ 找到 activity-service JAR文件" -ForegroundColor Green
    $size = (Get-Item $activityJar).Length / 1MB
    Write-Host "  文件大小: $([math]::Round($size, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "✗ 未找到 activity-service JAR文件" -ForegroundColor Red
    exit 1
}

# 检查Java环境
try {
    java -version | Out-Null
    Write-Host "✓ Java环境正常" -ForegroundColor Green
} catch {
    Write-Host "✗ Java环境异常" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "正在启动 activity-service (端口: 8081)..." -ForegroundColor Yellow
Write-Host "注意：此服务需要MySQL和Redis运行中" -ForegroundColor Yellow
Write-Host ""

# 设置环境变量并启动
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/dzvote?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "root123456"
$env:SPRING_REDIS_HOST = "localhost"
$env:SPRING_REDIS_PORT = "6379"

Write-Host "启动参数:" -ForegroundColor Cyan
Write-Host "  Profile: dev" -ForegroundColor White
Write-Host "  Database: localhost:3306/dzvote" -ForegroundColor White
Write-Host "  Redis: localhost:6379" -ForegroundColor White
Write-Host ""

try {
    Write-Host "启动服务中..." -ForegroundColor Blue
    Start-Process -FilePath "java" -ArgumentList "-jar", $activityJar -WorkingDirectory (Split-Path $activityJar -Parent) -PassThru
    Write-Host "✓ activity-service 启动命令已执行" -ForegroundColor Green
} catch {
    Write-Host "✗ 启动失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "请等待服务启动，然后访问:" -ForegroundColor Cyan
Write-Host "  http://localhost:8081/actuator/health" -ForegroundColor White
Write-Host "  http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "按Ctrl+C停止服务，或关闭此窗口" -ForegroundColor Yellow

# 保持窗口打开
try {
    while ($true) {
        Start-Sleep -Seconds 10
        try {
            $connection = New-Object System.Net.Sockets.TcpClient
            $connection.Connect("localhost", 8081)
            $connection.Close()
            Write-Host "$(Get-Date -Format 'HH:mm:ss') - 服务运行正常 ✓" -ForegroundColor Green
        } catch {
            Write-Host "$(Get-Date -Format 'HH:mm:ss') - 无法连接到服务 ✗" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "监控已停止" -ForegroundColor Yellow
}