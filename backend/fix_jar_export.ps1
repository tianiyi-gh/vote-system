# 清理 JAR 中的冲突类并添加 SimpleExportController

$jarPath = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-2.0.0.jar"
$tempDir = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\temp_jar_fix"

# 1. 备份原始 JAR
Copy-Item -Path $jarPath -Destination "$jarPath.backup" -Force

# 2. 提取 JAR 内容
Write-Host "提取 JAR 内容..."
if (Test-Path $tempDir) { Remove-Item -Path $tempDir -Recurse -Force }
Expand-Archive -Path $jarPath -DestinationPath $tempDir -Force

# 3. 删除冲突的类文件
Write-Host "删除冲突类..."
$conflictClass = "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller\AdminControllerSimpleExport.class"
if (Test-Path $conflictClass) {
    Remove-Item -Path $conflictClass -Force
    Write-Host "已删除 AdminControllerSimpleExport.class"
}

# 4. 确保 SimpleExportController 存在
$targetClass = "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller\SimpleExportController.class"
$sourceClass = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\classes\com\dzvote\vote\controller\SimpleExportController.class"
if (Test-Path $sourceClass -and -not (Test-Path $targetClass)) {
    Copy-Item -Path $sourceClass -Destination $targetClass -Force
    Write-Host "已添加 SimpleExportController.class"
}

# 5. 重新打包
Write-Host "重新打包..."
Compress-Archive -Path "$tempDir\*" -DestinationPath "$jarPath.new" -Force

# 6. 替换原 JAR
Move-Item -Path "$jarPath.new" -Destination $jarPath -Force

# 7. 清理
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "完成! JAR 已更新。"
