# 报表导出功能文档

## 概述

投票系统支持将活动数据导出为 Excel 或 CSV 格式的报表。

## 功能特性

### ✅ Excel 导出
- 支持 .xlsx 格式
- 自动列宽调整
- 美化的表头样式
- 支持 UTF-8 中文

### ✅ CSV 导出
- 支持 UTF-8 编码
- 自动转义特殊字符
- 适配 Excel 打开

### ✅ 报表类型

| 报表类型 | 说明 | 支持的导出格式 |
|---------|------|----------------|
| VOTE_RECORDS | 投票记录明细 | Excel, CSV |
| CANDIDATE_STATS | 候选人统计 | Excel, CSV |
| ACTIVITY_SUMMARY | 活动汇总 | Excel, CSV |

## API 接口

### 导出活动报表

```
POST /api/admin/export
Content-Type: application/json
```

**请求参数：**

```json
{
  "activityId": 1,
  "exportType": "EXCEL",
  "reportType": "VOTE_RECORDS",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

**参数说明：**

| 参数 | 类型 | 必填 | 说明 |
|-----|--------|--------|------|
| activityId | Long | 是 | 活动ID |
| exportType | String | 是 | 导出类型：EXCEL / CSV |
| reportType | String | 是 | 报表类型：VOTE_RECORDS / CANDIDATE_STATS / ACTIVITY_SUMMARY |
| startDate | String | 否 | 开始日期（格式：yyyy-MM-dd） |
| endDate | String | 否 | 结束日期（格式：yyyy-MM-dd） |

**响应：**

成功时直接下载文件（Content-Type: application/octet-stream 或对应类型）
失败时返回 JSON：
```json
{
  "success": false,
  "message": "活动不存在"
}
```

## 使用示例

### 示例 1：导出投票记录（Excel）

**PowerShell：**
```powershell
$body = @{
    activityId = 1
    exportType = "EXCEL"
    reportType = "VOTE_RECORDS"
} | ConvertTo-Json

$progressPreference = "SilentlyContinue"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-RestMethod -Uri "http://localhost:8082/api/admin/export" -Method POST `
  -Body $body -ContentType "application/json" `
  -OutFile "C:\temp\vote_records.xlsx"
```

**curl：**
```bash
curl -X POST "http://localhost:8082/api/admin/export" \
  -H "Content-Type: application/json" \
  -d '{"activityId": 1, "exportType": "EXCEL", "reportType": "VOTE_RECORDS"}' \
  --output vote_records.xlsx
```

### 示例 2：导出候选人统计（CSV）

**PowerShell：**
```powershell
$body = @{
    activityId = 1
    exportType = "CSV"
    reportType = "CANDIDATE_STATS"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/admin/export" -Method POST `
  -Body $body -ContentType "application/json" `
  -OutFile "C:\temp\candidate_stats.csv"
```

**curl：**
```bash
curl -X POST "http://localhost:8082/api/admin/export" \
  -H "Content-Type: application/json" \
  -d '{"activityId": 1, "exportType": "CSV", "reportType": "CANDIDATE_STATS"}' \
  --output candidate_stats.csv
```

### 示例 3：导出活动汇总

**PowerShell：**
```powershell
$body = @{
    activityId = 1
    exportType = "EXCEL"
    reportType = "ACTIVITY_SUMMARY"
    startDate = "2024-01-01"
    endDate = "2024-12-31"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/admin/export" -Method POST `
  -Body $body -ContentType "application/json" `
  -OutFile "C:\temp\activity_summary.xlsx"
```

## 报表字段说明

### VOTE_RECORDS（投票记录）

| 字段 | 说明 | 示例 |
|-----|------|--------|
| ID | 投票记录ID | 12345 |
| Activity ID | 活动ID | 1 |
| Candidate ID | 候选人ID | 10 |
| Voter Phone | 投票人手机号 | 138****1234 |
| Voter IP | 投票人IP | 192.168.1.1 |
| Vote Time | 投票时间 | 2024-01-08 12:30:45 |
| Channel | 投票渠道 | SMS/WEB/APP/WECHAT/PAY |
| Valid | 是否有效 | 1-有效 0-无效 |
| Device Fingerprint | 设备指纹 | abc123def456 |

### CANDIDATE_STATS（候选人统计）

| 字段 | 说明 | 示例 |
|-----|------|--------|
| ID | 候选人ID | 10 |
| Name | 候选人姓名 | 张三 |
| Description | 候选人描述 | 优秀员工 |
| Votes | 得票数 | 1234 |
| Activity ID | 活动ID | 1 |
| Status | 状态 | 1-启用 0-禁用 |

### ACTIVITY_SUMMARY（活动汇总）

| 字段 | 说明 | 示例 |
|-----|------|--------|
| Activity ID | 活动ID | 1 |
| Title | 活动标题 | 2024年度优秀员工评选 |
| Total Votes | 总票数 | 50000 |
| Candidate Count | 候选人数 | 20 |
| Start Time | 开始时间 | 2024-01-01 00:00:00 |
| End Time | 结束时间 | 2024-12-31 23:59:59 |
| Status | 活动状态 | 1-进行中 0-已结束 |

## 依赖说明

### Maven 依赖

```xml
<!-- Apache POI for Excel Export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.3</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

## 性能优化

### 大数据量导出优化建议

1. **分页导出**
   - 对于数据量超过 10 万条记录，建议分批次导出
   - 每批次最多导出 5 万条记录

2. **异步导出**
   - 对于复杂报表，建议使用异步任务
   - 提供下载链接而非直接返回文件

3. **缓存优化**
   - 相同参数的报表可以缓存
   - 缓存时间设置为 5 分钟

## 测试建议

### 单元测试
- 测试 Excel 格式是否正确
- 测试 CSV 编码是否正确
- 测试日期格式化
- 测试特殊字符转义

### 集成测试
- 测试小数据量（< 1000 条）
- 测试中等数据量（1000-10000 条）
- 测试大数据量（> 10000 条）
- 测试空数据
- 测试无效日期范围

## 常见问题

### Q1: 导出的 Excel 打开乱码？
A: 确保使用 UTF-8 编码，POI 库已正确设置字体。

### Q2: CSV 在 Excel 中显示为单列？
A: CSV 文件需要添加 BOM 头（EF BB BF），让 Excel 识别为 UTF-8 编码。

### Q3: 导出失败提示"活动不存在"？
A: 检查 activityId 是否正确，确认活动在数据库中存在。

### Q4: 大数据量导出超时？
A: 
- 增加日期范围限制
- 使用异步导出
- 优化 SQL 查询添加索引

## 文件清单

### 新增文件
```
新架构/backend/vote-service/src/main/java/com/dzvote/vote/
├── dto/
│   └── ReportExportRequest.java    # 报表导出请求DTO
├── service/
│   └── ExportService.java           # 报表导出服务
└── controller/
    └── AdminController.java         # 已更新，添加导出功能实现
```

### 修改文件
```
新架构/backend/vote-service/src/main/java/com/dzvote/vote/
└── mapper/
    └── VoteRecordMapper.java       # 新增查询投票记录方法
新架构/backend/vote-service/pom.xml              # 添加Apache POI依赖
```

## 下一步计划

- [ ] 实现自定义报表模板功能
- [ ] 实现定时自动报表生成
- [ ] 添加报表下载历史记录
- [ ] 优化大数据量导出性能
- [ ] 增加报表权限控制
