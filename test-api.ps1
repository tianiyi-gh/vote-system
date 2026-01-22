# API Testing Script
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       DZVOTE 2.0 - API Testing Script" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081"

# Function to test endpoint
function Test-Endpoint {
    param (
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [object]$Body = $null
    )
    
    Write-Host "Testing: $Name" -ForegroundColor Yellow
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            ContentType = "application/json"
            TimeoutSec = 10
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "  Status: SUCCESS" -ForegroundColor Green
        Write-Host "  Response:" -ForegroundColor Cyan
        $response | ConvertTo-Json -Depth 5 | Write-Host
        Write-Host ""
        return $true
    }
    catch {
        Write-Host "  Status: FAILED" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        return $false
    }
}

# Wait for service to start
Write-Host "Waiting for service to start..." -ForegroundColor Yellow
$retries = 0
$maxRetries = 30

while ($retries -lt $maxRetries) {
    try {
        $null = Invoke-RestMethod -Uri "$baseUrl/api/activities?page=1&size=10" -Method GET -TimeoutSec 2
        Write-Host "Service is ready!" -ForegroundColor Green
        Write-Host ""
        break
    }
    catch {
        $retries++
        Write-Host "  Waiting... ($retries/$maxRetries)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
}

if ($retries -eq $maxRetries) {
    Write-Host ""
    Write-Host "ERROR: Service did not start in time!" -ForegroundColor Red
    Write-Host "Please check if the service is running on port 8081" -ForegroundColor Yellow
    exit 1
}

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       Running API Tests" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get activities list
Test-Endpoint -Name "1. Get Activities List (Paginated)" `
    -Url "$baseUrl/api/activities?page=1&size=10"

# Test 2: Create new activity
$newActivity = @{
    title = "测试投票活动"
    region = "全国"
    description = "这是一个测试活动"
    startTime = (Get-Date).AddDays(1).ToString("yyyy-MM-dd HH:mm:ss")
    endTime = (Get-Date).AddDays(7).ToString("yyyy-MM-dd HH:mm:ss")
    maxVotes = 10
    status = 0
}

$created = Test-Endpoint -Name "2. Create New Activity" `
    -Url "$baseUrl/api/activities" `
    -Method "POST" `
    -Body $newActivity

# Test 3: Check Swagger documentation
Write-Host "Testing: 3. Swagger UI Access" -ForegroundColor Yellow
try {
    $swagger = Invoke-WebRequest -Uri "$baseUrl/swagger-ui.html" -Method GET -TimeoutSec 5
    if ($swagger.StatusCode -eq 200) {
        Write-Host "  Status: SUCCESS" -ForegroundColor Green
        Write-Host "  Swagger UI is accessible at: $baseUrl/swagger-ui.html" -ForegroundColor Cyan
    }
}
catch {
    Write-Host "  Status: FAILED" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 4: OpenAPI docs
Write-Host "Testing: 4. OpenAPI Documentation" -ForegroundColor Yellow
try {
    $openapi = Invoke-RestMethod -Uri "$baseUrl/v3/api-docs" -Method GET -TimeoutSec 5
    Write-Host "  Status: SUCCESS" -ForegroundColor Green
    Write-Host "  OpenAPI version: $($openapi.openapi)" -ForegroundColor Cyan
    Write-Host "  API title: $($openapi.info.title)" -ForegroundColor Cyan
}
catch {
    Write-Host "  Status: FAILED" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       Test Summary" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service Status: RUNNING" -ForegroundColor Green
Write-Host "Base URL: $baseUrl" -ForegroundColor White
Write-Host ""
Write-Host "Available Endpoints:" -ForegroundColor Cyan
Write-Host "  - API:     $baseUrl/api/activities" -ForegroundColor White
Write-Host "  - Swagger: $baseUrl/swagger-ui.html" -ForegroundColor White
Write-Host "  - OpenAPI: $baseUrl/v3/api-docs" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Open browser and visit Swagger UI" -ForegroundColor White
Write-Host "  2. Test more API endpoints" -ForegroundColor White
Write-Host "  3. Start developing Vue3 frontend" -ForegroundColor White
Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
