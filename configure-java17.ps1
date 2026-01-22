# Java 17 Configuration Script
# This script will find and configure Java 17

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       Java 17 Configuration Script" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Searching for Java 17 installation..." -ForegroundColor Yellow
Write-Host ""

# Common Java installation paths
$searchPaths = @(
    "D:\ide\Java\java17",
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Temurin",
    "C:\Program Files\Java",
    "C:\Program Files (x86)\Eclipse Adoptium",
    "C:\Program Files (x86)\Temurin",
    "C:\Program Files (x86)\Java"
)

$javaPath = $null

foreach ($path in $searchPaths) {
    if (Test-Path $path) {
        Write-Host "Checking: $path" -ForegroundColor Gray
        
        # Check if it's a direct JDK path
        if (Test-Path (Join-Path $path "bin\java.exe")) {
            $version = & (Join-Path $path "bin\java.exe") -version 2>&1 | Select-String -Pattern "17\."
            if ($version) {
                $javaPath = $path
                Write-Host "  Found Java 17!" -ForegroundColor Green
                break
            }
        }
        
        # Check subdirectories
        Get-ChildItem $path -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $subPath = $_.FullName
            if ($_.Name -like "*jdk-17*" -or $_.Name -like "*17.*") {
                if (Test-Path (Join-Path $subPath "bin\java.exe")) {
                    $javaPath = $subPath
                    Write-Host "  Found Java 17: $subPath" -ForegroundColor Green
                    return
                }
            }
        }
        
        if ($javaPath) { break }
    }
}

if (-not $javaPath) {
    Write-Host ""
    Write-Host "ERROR: Java 17 not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please run the installation script first:" -ForegroundColor Yellow
    Write-Host "  .\install-java17.ps1" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "Java 17 found at:" -ForegroundColor Green
Write-Host $javaPath -ForegroundColor White
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Verify Java version
$javaExe = Join-Path $javaPath "bin\java.exe"
Write-Host "Java Version:" -ForegroundColor Cyan
& $javaExe -version 2>&1
Write-Host ""

# Ask user if want to set as system default
Write-Host "Do you want to set this as system default Java? (Y/N)" -ForegroundColor Yellow
Write-Host "  Y - Set JAVA_HOME system environment variable" -ForegroundColor Gray
Write-Host "  N - Only set for current session" -ForegroundColor Gray
Write-Host ""
$choice = Read-Host "Your choice"

if ($choice -eq "Y" -or $choice -eq "y") {
    # Check Administrator privileges
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    
    if (-not $isAdmin) {
        Write-Host ""
        Write-Host "ERROR: Administrator privileges required to set system variables!" -ForegroundColor Red
        Write-Host "Please run this script as Administrator" -ForegroundColor Yellow
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "Setting system environment variables..." -ForegroundColor Yellow
        
        # Set JAVA_HOME
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, [System.EnvironmentVariableTarget]::Machine)
        Write-Host "  JAVA_HOME = $javaPath" -ForegroundColor Green
        
        # Update PATH
        $machinePath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
        $javaBinPath = Join-Path $javaPath "bin"
        
        # Remove old Java paths
        $pathParts = $machinePath -split ";"
        $newPathParts = $pathParts | Where-Object { $_ -notlike "*\jdk*\bin*" -and $_ -notlike "*\java*\bin*" }
        
        # Add new Java path at the beginning
        $newPath = "$javaBinPath;" + ($newPathParts -join ";")
        [System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)
        Write-Host "  PATH updated" -ForegroundColor Green
        
        Write-Host ""
        Write-Host "System environment variables updated successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "IMPORTANT: Please close ALL PowerShell windows and open a new one!" -ForegroundColor Yellow
    }
}

# Set for current session
Write-Host ""
Write-Host "Setting for current session..." -ForegroundColor Yellow
$env:JAVA_HOME = $javaPath
$env:Path = (Join-Path $javaPath "bin") + ";" + $env:Path

Write-Host "  Current session configured" -ForegroundColor Green
Write-Host ""

# Test current session
Write-Host "Current session test:" -ForegroundColor Cyan
Write-Host "  JAVA_HOME = $env:JAVA_HOME" -ForegroundColor White
java -version 2>&1

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       Configuration Complete!" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. If you set system variables, close and reopen PowerShell" -ForegroundColor White
Write-Host "2. Run: java -version" -ForegroundColor White
Write-Host "3. Notify the AI to upgrade the project to Java 17" -ForegroundColor White
Write-Host ""
