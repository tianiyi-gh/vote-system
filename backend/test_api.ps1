$headers = @{
    "Content-Type" = "application/json"
}

# 测试登录
Write-Host "测试登录接口..." -ForegroundColor Cyan
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json
$loginResult = Invoke-WebRequest -Uri "http://localhost:8084/api/auth/login" -Method POST -Headers $headers -Body $loginBody
Write-Host $loginResult.Content
Write-Host ""

# 测试投票
Write-Host "测试投票接口..." -ForegroundColor Cyan
$voteBody = @{
    activityId = 1
    candidateId = 1
    channel = "WEB"
    voterPhone = "13800138000"
    captcha = "1234"
    captchaKey = "test"
} | ConvertTo-Json
$voteResult = Invoke-WebRequest -Uri "http://localhost:8085/api/votes" -Method POST -Headers $headers -Body $voteBody
Write-Host $voteResult.Content
Write-Host ""

# 健康检查
Write-Host "健康检查..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "http://localhost:8084/api/auth/health" | ForEach-Object { Write-Host $_.Content }
Invoke-WebRequest -Uri "http://localhost:8085/api/votes/health" | ForEach-Object { Write-Host $_.Content }
