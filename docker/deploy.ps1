# PowerShell部署脚本
Write-Host "开始构建DZVote系统..." -ForegroundColor Green

# 检查Docker是否运行
try {
    docker version | Out-Null
    Write-Host "Docker运行正常" -ForegroundColor Green
} catch {
    Write-Host "错误: Docker未运行，请先启动Docker Desktop" -ForegroundColor Red
    exit 1
}

# 构建后端服务
Write-Host "构建后端服务..." -ForegroundColor Yellow
Set-Location "..\backend"

# 构建各个微服务
Write-Host "构建 activity-service..." -ForegroundColor Blue
Set-Location "activity-service"
try {
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven构建失败" }
} catch {
    Write-Host "activity-service构建失败" -ForegroundColor Red
    Set-Location "..\..\"
    exit 1
}
Set-Location ".."

Write-Host "构建 vote-service..." -ForegroundColor Blue
Set-Location "vote-service"
try {
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven构建失败" }
} catch {
    Write-Host "vote-service构建失败" -ForegroundColor Red
    Set-Location "..\..\"
    exit 1
}
Set-Location ".."

Write-Host "构建 statistics-service..." -ForegroundColor Blue
Set-Location "statistics-service"
try {
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven构建失败" }
} catch {
    Write-Host "statistics-service构建失败" -ForegroundColor Red
    Set-Location "..\..\"
    exit 1
}
Set-Location ".."

Write-Host "构建 gateway-service..." -ForegroundColor Blue
Set-Location "gateway-service"
try {
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven构建失败" }
} catch {
    Write-Host "gateway-service构建失败" -ForegroundColor Red
    Set-Location "..\..\"
    exit 1
}
Set-Location "..\.."

# 构建前端
Write-Host "构建前端管理后台..." -ForegroundColor Yellow
Set-Location "frontend\admin"
try {
    npm install
    npm run build
} catch {
    Write-Host "前端管理后台构建失败" -ForegroundColor Red
    Set-Location "..\..\"
    exit 1
}
Set-Location ".."

Write-Host "构建H5移动端..." -ForegroundColor Yellow
Set-Location "h5"
try {
    npm install
    npm run build
} catch {
    Write-Host "H5移动端构建失败" -ForegroundColor Red
    Set-Location "..\..\"
    exit 1
}
Set-Location "..\.."

# 启动Docker容器
Write-Host "启动Docker容器..." -ForegroundColor Yellow
Set-Location "docker"
try {
    docker-compose down
    docker-compose up -d --build
    if ($LASTEXITCODE -ne 0) { throw "Docker启动失败" }
} catch {
    Write-Host "Docker容器启动失败" -ForegroundColor Red
    exit 1
}

Write-Host "构建完成！" -ForegroundColor Green
Write-Host "管理后台地址: http://localhost" -ForegroundColor Cyan
Write-Host "H5移动端地址: http://localhost:81" -ForegroundColor Cyan
Write-Host "API网关地址: http://localhost:8084" -ForegroundColor Cyan

# 等待服务启动
Write-Host "等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 检查服务状态
Write-Host "检查服务状态..." -ForegroundColor Yellow
docker-compose ps

Write-Host "部署完成！请查看服务状态确认所有服务都已正常启动。" -ForegroundColor Green