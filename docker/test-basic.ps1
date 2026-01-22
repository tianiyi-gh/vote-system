# 基础环境测试和启动
Write-Host "=== DZVote环境测试 ===" -ForegroundColor Cyan

# 检查基础环境
Write-Host "检查环境..." -ForegroundColor Yellow
try { java -version | Out-Null; Write-Host "✅ Java OK" -ForegroundColor Green } catch { Write-Host "❌ Java 缺失" -ForegroundColor Red }
try { mvn -version | Out-Null; Write-Host "✅ Maven OK" -ForegroundColor Green } catch { Write-Host "❌ Maven 缺失" -ForegroundColor Red }
try { node --version | Out-Null; Write-Host "✅ Node.js OK" -ForegroundColor Green } catch { Write-Host "❌ Node.js 缺失" -ForegroundColor Red }
try { docker --version | Out-Null; Write-Host "✅ Docker OK" -ForegroundColor Green } catch { Write-Host "❌ Docker 缺失" -ForegroundColor Red }

Write-Host "`n=== 启动简化版Activity Service ===" -ForegroundColor Cyan

# 直接使用我们已有的简化版本
$jarPath = "..\backend\activity-service\target\activity-service-2.0.0.jar"
if (Test-Path $jarPath) {
    Write-Host "✅ 找到JAR文件，启动服务..." -ForegroundColor Green
    Write-Host "访问地址:" -ForegroundColor White
    Write-Host "  健康检查: http://localhost:8081/test/health" -ForegroundColor Cyan
    Write-Host "  API文档: http://localhost:8081/swagger-ui.html" -ForegroundColor Cyan
    Write-Host "`n按Ctrl+C停止服务`n" -ForegroundColor Gray
    
    Set-Location (Split-Path $jarPath -Parent)
    java -jar "activity-service-2.0.0.jar" --spring.profiles.active=test
} else {
    Write-Host "❌ JAR文件不存在，需要先构建" -ForegroundColor Red
    Write-Host "运行: cd ../backend/activity-service && mvn package -DskipTests" -ForegroundColor Yellow
}