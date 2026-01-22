# 测试导出 API

$baseUrl = "http://localhost:8082"

# 测试 1: 获取活动列表
Write-Host "测试 1: 获取活动列表..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/activities" -Method GET
    Write-Host "成功获取活动列表，活动数量: $($response.data.Count)"
    $activityId = $response.data[0].id
    Write-Host "使用活动 ID: $activityId"
} catch {
    Write-Host "获取活动列表失败: $_"
    exit 1
}

# 测试 2: 尝试导出（当前会返回"开发中"）
Write-Host "`n测试 2: 尝试导出候选人统计 (CSV)..."
$body = @{
    activityId = $activityId
    exportType = "CSV"
    reportType = "CANDIDATE_STATS"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/export" -Method POST -Body $body -ContentType "application/json"
    Write-Host "响应: $($response | ConvertTo-Json)"
} catch {
    Write-Host "导出请求失败: $_"
    if ($_.ErrorDetails) {
        Write-Host "错误详情: $($_.ErrorDetails)"
    }
}

Write-Host "`n测试完成!"
