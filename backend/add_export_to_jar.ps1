# 添加导出控制器到现有 JAR

$jarPath = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-2.0.0.jar"
$tempDir = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\temp_jar_extract"
$sourceFile = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\src\main\java\com\dzvote\vote\controller\AdminControllerSimpleExport.java"

# 1. 提取 JAR 内容
Write-Host "提取 JAR 内容..."
if (Test-Path $tempDir) { Remove-Item -Path $tempDir -Recurse -Force }
Expand-Archive -Path $jarPath -DestinationPath $tempDir -Force

# 2. 编译新控制器
Write-Host "编译控制器..."
$compileCmd = "javac -encoding UTF-8 -cp '$jarPath' -d '$tempDir\BOOT-INF\classes' '$sourceFile'"
Invoke-Expression $compileCmd

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败!"
    exit 1
}

# 3. 重新打包
Write-Host "重新打包..."
$backupJar = $jarPath + ".backup"
Copy-Item -Path $jarPath -Destination $backupJar -Force

Compress-Archive -Path "$tempDir\*" -DestinationPath "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-new.zip" -Force
Move-Item -Path "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-new.zip" -Destination $jarPath -Force

# 清理
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "完成! JAR 已更新."
