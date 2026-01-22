# 报表导出功能部署指南

## 当前状态

### 已完成
1. **前端导出功能** ✓
   - 统计页面：支持导出投票记录
   - 候选人页面：支持导出候选人统计
   - API 配置：`admin.export` 方法

2. **后端源代码** ✓
   - `ExportService.java`: Excel/CSV 导出服务
   - `AdminController.java`: 导出 API 端点（依赖 ExportService）
   - `SimpleExportController.java`: 简化版导出（不依赖 POI）

### 问题
现有 JAR (`vote-service-2.0.0.jar`) 中：
- `AdminController` 的导出方法返回"开发中"
- `ExportService` 未编译到 JAR

## 解决方案

### 方案 A: 使用简化导出接口（快速方案）

1. 编译 `SimpleExportController`：
```bash
cd d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service
javac -encoding UTF-8 -cp target/vote-service-2.0.0.jar ^
    -d target/classes ^
    src/main/java/com/dzvote/vote/controller/SimpleExportController.java
```

2. 添加到 JAR：
```bash
jar uf target/vote-service-2.0.0.jar ^
    -C target/classes com/dzvote/vote/controller/SimpleExportController.class
```

3. 重启服务
```bash
taskkill /F /IM java.exe
java -jar target/vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082
```

4. 测试新接口：
```powershell
# 导出候选人统计
Invoke-WebRequest -Uri "http://localhost:8082/api/export/simple/candidates/3" -OutFile "candidates.csv"

# 导出活动汇总
Invoke-WebRequest -Uri "http://localhost:8082/api/export/simple/activity/3" -OutFile "activity.csv"
```

### 方案 B: 完整 Maven 重新编译（推荐）

```bash
cd d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service
D:/ide/maven3.9/bin/mvn.cmd clean package -DskipTests
```

## 前端适配

如果使用简化导出接口，需要修改前端代码：

### 1. 更新 API 配置 (`admin/src/api/index.ts`)

```typescript
// 临时使用简化接口
admin: {
  exportSimple: {
    candidates: (activityId: number) => get(`/api/export/simple/candidates/${activityId}`),
    activity: (activityId: number) => get(`/api/export/simple/activity/${activityId}`)
  }
}
```

### 2. 修改统计页面 (`admin/src/views/statistics/index.vue`)

```typescript
const downloadExport = async (exportType: string, extension: string) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在导出...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 使用简化接口（仅 CSV）
    const response = await fetch(`http://localhost:8082/api/export/simple/candidates/${filterForm.activityId}`)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `candidates_${filterForm.activityId}_${Date.now()}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)

    loading.close()
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error('导出失败，请重试')
  }
}
```

## 测试步骤

### 1. 测试后端服务
```powershell
# 检查服务状态
Invoke-WebRequest -Uri "http://localhost:8082/api/activities" -UseBasicParsing

# 测试导出候选人
Invoke-WebRequest -Uri "http://localhost:8082/api/export/simple/candidates/3" -OutFile "test.csv"
Get-Content "test.csv"
```

### 2. 测试前端界面
```bash
cd d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/frontend/admin
npm run dev
```

访问：http://localhost:5173

### 3. 前后端联调
1. 进入"候选人管理"页面
2. 点击"导出统计"按钮
3. 验证 CSV 文件下载

## 下一步优化

1. **Excel 导出支持**
   - 将 Apache POI 依赖正确添加到 classpath
   - 重新编译包含 ExportService 的 JAR

2. **异步导出**
   - 实现导出任务队列
   - 添加下载链接而非直接返回文件

3. **导出历史记录**
   - 记录导出操作
   - 提供历史文件下载

4. **权限控制**
   - 仅管理员可导出
   - 记录导出日志

## 文件清单

### 新增/修改的文件
```
新架构/backend/vote-service/src/main/java/com/dzvote/vote/
├── service/ExportService.java                    # 导出服务（需编译）
├── controller/AdminController.java               # 主导出控制器（需编译）
├── controller/SimpleExportController.java        # 简化导出控制器（推荐）
└── mapper/VoteRecordMapper.java                # 修复了 getCandidateVoteRates

新架构/frontend/admin/src/
├── api/index.ts                               # 新增 admin.export
├── views/statistics/index.vue                  # 导出投票记录
└── views/candidate/list.vue                   # 导出候选人统计
```
