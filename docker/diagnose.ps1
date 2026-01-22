# 系统环境诊断脚本
Write-Host "=== DZVote系统环境诊断 ===" -ForegroundColor Cyan
Write-Host ""

# 检查操作系统
Write-Host "操作系统信息:" -ForegroundColor Yellow
Write-Host "  OS: $((Get-WmiObject -class Win32_OperatingSystem).Caption)"
Write-Host "  Architecture: $env:PROCESSOR_ARCHITECTURE"
Write-Host ""

# 检查Java环境
Write-Host "Java环境检查:" -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "  ✓ Java已安装" -ForegroundColor Green
    Write-Host "    $javaVersion"
    
    # 检查Java版本
    $versionMatch = $javaVersion | Select-String 'version "(\d+)\.(\d+)'
    if ($versionMatch) {
        $majorVersion = [int]$versionMatch.Matches[0].Groups[1].Value
        if ($majorVersion -lt 17) {
            Write-Host "  ⚠ Java版本较低，建议使用Java 17或更高版本" -ForegroundColor Yellow
        } else {
            Write-Host "  ✓ Java版本符合要求" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "  ✗ Java未安装或未配置到PATH" -ForegroundColor Red
    Write-Host "  请下载安装Java 17+: https://adoptium.net/" -ForegroundColor Yellow
}
Write-Host ""

# 检查Maven环境
Write-Host "Maven环境检查:" -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1
    Write-Host "  ✓ Maven已安装" -ForegroundColor Green
    $mvnVersion | Select-String "Apache Maven" | ForEach-Object { Write-Host "    $_" }
} catch {
    Write-Host "  ✗ Maven未安装或未配置到PATH" -ForegroundColor Red
    Write-Host "  请下载安装Maven: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
}
Write-Host ""

# 检查Node.js环境
Write-Host "Node.js环境检查:" -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "  ✓ Node.js已安装: $nodeVersion" -ForegroundColor Green
    
    $npmVersion = npm --version
    Write-Host "  ✓ npm已安装: $npmVersion" -ForegroundColor Green
    
    if ([version]$nodeVersion.Replace('v','') -lt [version]'18.0.0') {
        Write-Host "  ⚠ Node.js版本较低，建议使用18.0或更高版本" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Node.js未安装或未配置到PATH" -ForegroundColor Red
    Write-Host "  请下载安装Node.js: https://nodejs.org/" -ForegroundColor Yellow
}
Write-Host ""

# 检查Docker环境
Write-Host "Docker环境检查:" -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "  ✓ Docker已安装: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Docker未安装或未启动" -ForegroundColor Red
    Write-Host "  请下载安装Docker Desktop: https://www.docker.com/products/docker-desktop/" -ForegroundColor Yellow
}

try {
    $composeVersion = docker-compose --version
    Write-Host "  ✓ Docker Compose已安装: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Docker Compose未安装" -ForegroundColor Red
    Write-Host "  注意：新版Docker Desktop已内置docker-compose，尝试使用'docker compose'命令" -ForegroundColor Yellow
}
Write-Host ""

# 检查端口占用
Write-Host "端口占用检查:" -ForegroundColor Yellow
$ports = @(80, 81, 3306, 6379, 8081, 8082, 8083, 8084)
foreach ($port in $ports) {
    try {
        $connection = New-Object System.Net.Sockets.TcpClient
        $connection.Connect("localhost", $port)
        $connection.Close()
        Write-Host "  ⚠ 端口 $port 已被占用" -ForegroundColor Yellow
    } catch {
        Write-Host "  ✓ 端口 $port 可用" -ForegroundColor Green
    }
}
Write-Host ""

# 检查项目文件
Write-Host "项目文件检查:" -ForegroundColor Yellow
$projectPaths = @(
    "..\backend\pom.xml",
    "..\backend\activity-service\pom.xml",
    "..\backend\vote-service\pom.xml",
    "..\backend\statistics-service\pom.xml",
    "..\backend\gateway-service\pom.xml",
    "..\frontend\admin\package.json",
    "..\frontend\h5\package.json",
    "..\docker-compose.yml"
)

foreach ($path in $projectPaths) {
    if (Test-Path $path) {
        Write-Host "  ✓ $path" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $path 缺失" -ForegroundColor Red
    }
}
Write-Host ""

# 检查构建产物
Write-Host "构建产物检查:" -ForegroundColor Yellow
$jarPaths = @(
    "..\backend\activity-service\target\activity-service-2.0.0.jar",
    "..\backend\vote-service\target\vote-service-1.0.0.jar",
    "..\backend\statistics-service\target\statistics-service-1.0.0.jar",
    "..\backend\gateway-service\target\gateway-service-1.0.0.jar"
)

foreach ($path in $jarPaths) {
    if (Test-Path $path) {
        $size = (Get-Item $path).Length / 1MB
        Write-Host "  ✓ $path ($([math]::Round($size, 2)) MB)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $path 缺失" -ForegroundColor Red
    }
}
Write-Host ""

# 生成诊断报告
Write-Host "=== 诊断完成 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "建议的启动方式:" -ForegroundColor Green
Write-Host "1. 如果Docker环境正常: .\deploy.ps1" -ForegroundColor White
Write-Host "2. 如果Docker不可用但Java环境正常: .\start-services.ps1" -ForegroundColor White
Write-Host "3. 手动启动：先启动MySQL/Redis，再逐个启动Java服务" -ForegroundColor White
Write-Host ""
Write-Host "常见问题解决:" -ForegroundColor Yellow
Write-Host "- Java安装问题：确保JAVA_HOME环境变量设置正确" -ForegroundColor White
Write-Host "- Docker问题：启动Docker Desktop，检查WSL2设置" -ForegroundColor White
Write-Host "- 端口冲突：关闭占用端口的服务或修改配置端口" -ForegroundColor White
Write-Host "- 构建失败：检查网络连接和Maven配置" -ForegroundColor White