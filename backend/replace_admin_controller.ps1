# 替换 AdminController 为 AdminExportClean

$jarPath = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-2.0.0.jar"
$tempDir = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\temp_replace"
$sourceController = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\classes\com\dzvote\vote\controller\AdminExportClean.class"

# 检查源文件是否存在
if (-not (Test-Path $sourceController)) {
    Write-Host "错误: AdminExportClean.class 不存在，需要先编译"
    exit 1
}

# 1. 备份
Write-Host "备份 JAR..."
Copy-Item -Path $jarPath -Destination "$jarPath.backup2" -Force

# 2. 提取
Write-Host "提取 JAR..."
if (Test-Path $tempDir) { Remove-Item -Path $tempDir -Recurse -Force }
Expand-Archive -Path $jarPath -DestinationPath $tempDir -Force

# 3. 删除旧的 AdminController
Write-Host "删除旧 AdminController..."
$oldController = "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller\AdminController.class"
if (Test-Path $oldController) {
    Remove-Item -Path $oldController -Force
}

# 4. 删除冲突类
Write-Host "删除冲突类..."
$conflictClasses = @(
    "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller\AdminControllerSimpleExport.class",
    "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller\SimpleExportController.class"
)
foreach ($file in $conflictClasses) {
    if (Test-Path $file) {
        Remove-Item -Path $file -Force
        Write-Host "已删除: $(Split-Path $file -Leaf)"
    }
}

# 5. 添加新的 AdminExportClean
Write-Host "添加 AdminExportClean..."
$targetDir = "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller"
if (-not (Test-Path $targetDir)) {
    New-Item -Path $targetDir -ItemType Directory -Force
}
Copy-Item -Path $sourceController -Destination "$targetDir\AdminExportClean.class" -Force

# 6. 重新打包
Write-Host "重新打包..."
$zipPath = "$jarPath.new"
Compress-Archive -Path "$tempDir\*" -DestinationPath $zipPath -Force

# 7. 替换
Move-Item -Path $zipPath -Destination $jarPath -Force

# 8. 清理
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "完成! JAR 已更新。"
Write-Host "现在可以启动服务测试导出功能。"
