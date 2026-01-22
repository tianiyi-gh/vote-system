# 不依赖Docker的服务启动脚本（用于测试）
Write-Host "启动DZVote微服务测试环境..." -ForegroundColor Green

# 检查Java环境
try {
    java -version
    Write-Host "Java环境正常" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Java环境，请安装Java 17或更高版本" -ForegroundColor Red
    exit 1
}

# 检查Maven环境
try {
    mvn -version
    Write-Host "Maven环境正常" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Maven环境，请安装Maven" -ForegroundColor Red
    exit 1
}

$services = @(
    @{name="activity-service"; port=8081; jar="activity-service-2.0.0.jar"},
    @{name="vote-service"; port=8082; jar="vote-service-1.0.0.jar"},
    @{name="statistics-service"; port=8083; jar="statistics-service-1.0.0.jar"},
    @{name="gateway-service"; port=8084; jar="gateway-service-1.0.0.jar"}
)

Write-Host "检查各服务构建状态..." -ForegroundColor Yellow

foreach ($service in $services) {
    $jarPath = "..\backend\$($service.name)\target\$($service.jar)"
    if (Test-Path $jarPath) {
        Write-Host "✓ $($service.name) 构建完成" -ForegroundColor Green
    } else {
        Write-Host "✗ $($service.name) 未找到JAR文件: $jarPath" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "启动微服务（按Ctrl+C停止）..." -ForegroundColor Yellow
Write-Host "注意：此脚本需要MySQL和Redis服务已启动" -ForegroundColor Yellow

# 创建启动函数
function Start-Service {
    param($serviceName, $port, $jarFile)
    
    Write-Host "启动 $serviceName (端口: $port)..." -ForegroundColor Blue
    
    $process = Start-Process -FilePath "java" -ArgumentList "-jar", $jarFile -WorkingDirectory "..\backend\$serviceName\target" -PassThru -WindowStyle Minimized
    
    # 等待服务启动
    Start-Sleep -Seconds 10
    
    # 检查端口是否被占用
    try {
        $connection = New-Object System.Net.Sockets.TcpClient
        $connection.Connect("localhost", $port)
        $connection.Close()
        Write-Host "✓ $serviceName 启动成功" -ForegroundColor Green
        return $process
    } catch {
        Write-Host "✗ $serviceName 启动失败" -ForegroundColor Red
        $process.Kill()
        return $null
    }
}

# 依次启动服务
$processes = @()
foreach ($service in $services) {
    $jarPath = "..\backend\$($service.name)\target\$($service.jar)"
    if (Test-Path $jarPath) {
        $process = Start-Service -serviceName $service.name -port $service.port -jarFile $service.jar
        if ($process -ne $null) {
            $processes += $process
        }
    }
    Start-Sleep -Seconds 5
}

Write-Host ""
Write-Host "服务状态检查..." -ForegroundColor Yellow
foreach ($service in $services) {
    try {
        $connection = New-Object System.Net.Sockets.TcpClient
        $connection.Connect("localhost", $service.port)
        $connection.Close()
        Write-Host "✓ $($service.name):http://localhost:$($service.port)" -ForegroundColor Green
    } catch {
        Write-Host "✗ $($service.name):端口 $($service.port) 无法访问" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "按任意键停止所有服务..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# 停止所有服务
foreach ($process in $processes) {
    try {
        $process.Kill()
        Write-Host "已停止服务进程" -ForegroundColor Yellow
    } catch {
        Write-Host "停止进程时出错" -ForegroundColor Red
    }
}

Write-Host "所有服务已停止" -ForegroundColor Green