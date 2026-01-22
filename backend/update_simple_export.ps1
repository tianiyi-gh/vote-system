# 更新 SimpleExportController 到 JAR

$jarPath = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-2.0.0.jar"
$classFile = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\classes\com\dzvote\vote\controller\SimpleExportController.class"

Write-Host "更新 SimpleExportController 到 JAR..."

# 备份原 JAR
Copy-Item $jarPath -Destination "$jarPath.backup" -Force

# 提取 JAR
$tempDir = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\temp_jar"
if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }
Expand-Archive -Path $jarPath -DestinationPath $tempDir -Force

# 更新类文件
Copy-Item $classFile -Destination "$tempDir\BOOT-INF\classes\com\dzvote\vote\controller\" -Force

# 重新打包
Compress-Archive -Path "$tempDir\*" -DestinationPath $jarPath -Force

# 清理
Remove-Item $tempDir -Recurse -Force

Write-Host "更新完成！"
