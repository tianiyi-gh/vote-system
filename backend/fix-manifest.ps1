# PowerShell script to fix JAR manifest files
$ErrorActionPreference = "Stop"

function Fix-JarManifest {
    param(
        [string]$JarPath,
        [string]$StartClass
    )

    Write-Host "正在处理 $JarPath" -ForegroundColor Cyan

    # 创建临时目录
    $tempDir = Join-Path $env:TEMP "jar-fix-$(Get-Date -Format 'yyyyMMddHHmmss')"
    New-Item -Path $tempDir -ItemType Directory -Force | Out-Null

    try {
        # 解压 JAR
        Write-Host "解压 JAR 文件..."
        Add-Type -Assembly System.IO.Compression.FileSystem

        # 使用绝对路径
        $jarFullPath = Resolve-Path $JarPath -ErrorAction Stop
        [System.IO.Compression.ZipFile]::ExtractToDirectory($jarFullPath.Path, $tempDir)

        # 读取并修改 MANIFEST.MF
        $manifestPath = Join-Path $tempDir "META-INF\MANIFEST.MF"
        $manifestContent = Get-Content $manifestPath -Raw -Encoding UTF8

        # 替换 Start-Class
        $newManifest = $manifestContent -replace "Start-Class: .*", "Start-Class: $StartClass"

        # 写回 MANIFEST.MF
        Set-Content -Path $manifestPath -Value $newManifest -NoNewline -Encoding UTF8
        Write-Host "✓ 已设置 Start-Class 为: $StartClass" -ForegroundColor Green

        # 删除旧 JAR
        Remove-Item $JarPath -Force

        # 重新打包
        Write-Host "重新打包 JAR 文件..."
        $files = Get-ChildItem -Path $tempDir -Recurse -File
        $zip = [System.IO.Compression.ZipFile]::Open($JarPath, [System.IO.Compression.ZipArchiveMode]::Create)
        try {
            foreach ($file in $files) {
                $relativePath = $file.FullName.Substring($tempDir.Length + 1)
                $entry = $zip.CreateEntry($relativePath.Replace('\', '/'))
                $stream = $file.OpenRead()
                try {
                    $entryStream = $entry.Open()
                    try {
                        $stream.CopyTo($entryStream)
                    } finally {
                        $entryStream.Dispose()
                    }
                } finally {
                    $stream.Dispose()
                }
            }
        } finally {
            $zip.Dispose()
        }

        Write-Host "✓ 成功创建 $JarPath" -ForegroundColor Green

        # 验证
        Write-Host "验证 MANIFEST.MF..."
        Add-Type -Assembly System.IO.Compression.FileSystem
        $zipCheck = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
        try {
            $manifestEntry = $zipCheck.GetEntry("META-INF/MANIFEST.MF")
            $stream = $manifestEntry.Open()
            try {
                $reader = New-Object System.IO.StreamReader($stream)
                try {
                    $content = $reader.ReadToEnd()
                    if ($content -match "Start-Class: $StartClass") {
                        Write-Host "✓ Start-Class 验证通过" -ForegroundColor Green
                    } else {
                        Write-Host "✗ Start-Class 验证失败!" -ForegroundColor Red
                    }
                } finally {
                    $reader.Dispose()
                }
            } finally {
                $stream.Dispose()
            }
        } finally {
            $zipCheck.Dispose()
        }

    } finally {
        # 清理临时目录
        if (Test-Path $tempDir) {
            Remove-Item $tempDir -Recurse -Force
        }
    }
}

# 修复 user-service
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "修复 user-service" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow
$userJarPath = Join-Path $PSScriptRoot "user-service\target\user-service-2.0.0.jar"
Fix-JarManifest -JarPath $userJarPath -StartClass "com.dzvote.user.UserApplication"

# 修复 vote-service
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "修复 vote-service" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow
$voteJarPath = Join-Path $PSScriptRoot "vote-service\target\vote-service-2.0.0.jar"
Fix-JarManifest -JarPath $voteJarPath -StartClass "com.dzvote.vote.VoteServiceApplication"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "所有 JAR 文件已修复完成！" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green
