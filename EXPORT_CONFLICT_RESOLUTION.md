# 导出功能冲突问题解决方案

## 问题描述
启动服务时报错：
```
Ambiguous mapping. Cannot map 'adminControllerSimpleExport' method
com.dzvote.vote.controller.AdminControllerSimpleExport#exportActivityReport(Map, HttpServletResponse) 
to {POST [/api/admin/export]}: There is already 'adminController' bean method
com.dzvote.vote.controller.AdminController#exportActivityReport(ReportExportRequest, HttpServletResponse) mapped.
```

## 原因
两个控制器都映射到相同的路径 `/api/admin/export`：
1. `AdminController.java` - 主控制器
2. `AdminControllerSimpleExport.java` - 简化导出控制器

## 已删除的文件
```
d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/src/main/java/com/dzvote/vote/controller/AdminControllerSimpleExport.java
d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/src/main/java/com/dzvote/vote/controller/AdminExportClean.java
```

## 当前可用控制器

### AdminController.java
- 路径: `/api/admin/*`
- 状态: 占位代码（返回"导出功能开发中"）

### SimpleExportController.java
- 路径: `/api/export/simple/*`
- 功能: 简化 CSV 导出
- 接口:
  - GET `/api/export/simple/candidates/{activityId}` - 导出候选人
  - GET `/api/export/simple/activity/{activityId}` - 导出活动汇总

**注意**: AdminExportClean.java 也存在相同的路径冲突，已删除。现在使用 AdminController 和 SimpleExportController，它们映射到不同路径，不再冲突。

## 解决方案

### 方案 A: 使用 SimpleExportController（推荐）

前端修改 API 调用，使用简化导出接口：

```typescript
// 修改 frontend/admin/src/api/index.ts
export const api = {
  export: {
    candidates: (activityId: number) => get(`/api/export/simple/candidates/${activityId}`),
    activity: (activityId: number) => get(`/api/export/simple/activity/${activityId}`)
  }
}
```

```typescript
// 修改 frontend/admin/src/views/candidate/list.vue
const exportCandidateStats = async () => {
  // 直接使用 GET 请求
  const response = await fetch(`http://localhost:8082/api/export/simple/candidates/${activityId}`)
  const blob = await response.blob()
  // ... 下载逻辑
}
```

### 方案 B: 编译 AdminExportClean 并替换 JAR

1. 编译新控制器：
```bash
D:\ide\Java\jdk1.8\bin\javac -encoding UTF-8 ^
  -cp d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar ^
  -d d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes ^
  d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/src/main/java/com/dzvote/vote/controller/AdminExportClean.java
```

2. 手动替换 JAR 中的类：
```powershell
# 1. 提取 JAR
Expand-Archive -Path vote-service-2.0.0.jar -DestinationPath temp_extract -Force

# 2. 删除旧 AdminController
Remove-Item temp_extract\BOOT-INF\classes\com\dzvote\vote\controller\AdminController.class

# 3. 删除冲突类
Remove-Item temp_extract\BOOT-INF\classes\com\dzvote\vote\controller\AdminControllerSimpleExport.class
Remove-Item temp_extract\BOOT-INF\classes\com\dzvote\vote\controller\SimpleExportController.class

# 4. 复制新类
Copy-Item target\classes\com\dzvote\vote\controller\AdminExportClean.class `
  -Destination temp_extract\BOOT-INF\classes\com\dzvote\vote\controller\AdminExportClean.class

# 5. 重新打包
Compress-Archive -Path temp_extract\* -DestinationPath vote-service-2.0.0.jar -Force

# 6. 清理
Remove-Item temp_extract -Recurse -Force
```

3. 重启服务：
```bash
java -jar target/vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082
```

### 方案 C: 使用 Maven 完整编译（最佳）

```bash
cd d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service
D:/ide/maven3.9/bin/mvn.cmd clean package -DskipTests
java -jar target/vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082
```

## 前端适配

### 修改 candidate/list.vue

```typescript
const exportCandidateStats = async () => {
  ElMessageBox.confirm('选择导出格式', '导出候选人统计', {
    confirmButtonText: 'CSV',
    cancelButtonText: '取消',
    distinguishCancelAndClose: true,
  }).then(async () => {
    await downloadCandidateExport()
  })
}

const downloadCandidateExport = async () => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在导出...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 使用简化接口（GET 请求）
    const response = await fetch(`http://localhost:8082/api/export/simple/candidates/1`)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `candidate_stats_${Date.now()}.csv`
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

### 修改 statistics/index.vue

```typescript
const exportData = async () => {
  ElMessageBox.confirm('导出投票记录', '导出报表', {
    confirmButtonText: 'CSV',
    cancelButtonText: '取消',
  }).then(async () => {
    await downloadExport()
  })
}

const downloadExport = async () => {
  // 由于简化接口不支持投票记录导出，暂时提示
  ElMessage.info('投票记录导出正在开发中，请使用候选人导出功能')
}
```

## 测试步骤

### 1. 使用简化接口测试

```powershell
# 导出候选人统计
Invoke-WebRequest -Uri "http://localhost:8082/api/export/simple/candidates/3" -OutFile "candidates.csv"
Get-Content "candidates.csv"

# 导出活动汇总
Invoke-WebRequest -Uri "http://localhost:8082/api/export/simple/activity/3" -OutFile "activity.csv"
Get-Content "activity.csv"
```

### 2. 启动前端测试

```bash
cd d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/frontend/admin
npm run dev
```

访问 http://localhost:5173，测试候选人导出功能。

## 总结

| 方案 | 难度 | 功能完整度 | 推荐度 |
|------|--------|------------|---------|
| A: 使用 SimpleExportController | 低 | 中 | ⭐⭐⭐⭐ |
| B: 手动替换 JAR | 中 | 高 | ⭐⭐⭐ |
| C: Maven 完整编译 | 高 | 完整 | ⭐⭐⭐⭐⭐ |

建议先使用**方案 A**快速验证功能，后续使用**方案 C**完整部署。
