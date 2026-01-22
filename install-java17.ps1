# Java 17 Auto Installation Script
# This script will install Java 17 using Chocolatey

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       Java 17 Auto Installation Script" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Check Administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: Please run as Administrator!" -ForegroundColor Red
    Write-Host "Press Win+X and select 'Windows PowerShell (Admin)'" -ForegroundColor Yellow
    exit 1
}

Write-Host "[Step 1/4] Checking Chocolatey..." -ForegroundColor Yellow

# Check if Chocolatey is installed
$chocoInstalled = Get-Command choco -ErrorAction SilentlyContinue

if (-not $chocoInstalled) {
    Write-Host "Chocolatey not found, installing..." -ForegroundColor Yellow
    Write-Host ""
    
    # Install Chocolatey
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    
    # Refresh environment
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    
    Write-Host "Chocolatey installed successfully!" -ForegroundColor Green
} else {
    Write-Host "Chocolatey already installed" -ForegroundColor Green
}

Write-Host ""
Write-Host "[Step 2/4] Installing Java 17..." -ForegroundColor Yellow
Write-Host "This may take 5-10 minutes, please wait..." -ForegroundColor Cyan
Write-Host "Installation directory: D:\ide\Java\java17" -ForegroundColor Cyan
Write-Host ""

# Create installation directory if not exists
$installDir = "D:\ide\Java\java17"
if (-not (Test-Path "D:\ide\Java")) {
    New-Item -ItemType Directory -Path "D:\ide\Java" -Force | Out-Null
}

# Install Java 17 to custom directory
choco install temurin17 -y --install-arguments="INSTALLDIR=`"$installDir`" /quiet"

Write-Host ""
Write-Host "[Step 3/4] Configuring environment variables..." -ForegroundColor Yellow

# Get Java installation path
$javaPath = "D:\ide\Java\java17"
if (-not (Test-Path $javaPath)) {
    # Fallback to default Chocolatey installation path
    Write-Host "Custom path not found, checking default installation..." -ForegroundColor Yellow
    $defaultPath = "C:\Program Files\Eclipse Adoptium"
    if (Test-Path $defaultPath) {
        $javaPath = Get-ChildItem $defaultPath -Directory | Where-Object { $_.Name -like "jdk-17*" } | Select-Object -First 1 -ExpandProperty FullName
    }
}

if ($javaPath) {
    # Set JAVA_HOME
    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, [System.EnvironmentVariableTarget]::Machine)
    
    # Add to PATH
    $machinePath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
    $javaBinPath = Join-Path $javaPath "bin"
    
    if ($machinePath -notlike "*$javaBinPath*") {
        $newPath = "$javaBinPath;$machinePath"
        [System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)
    }
    
    # Refresh current session
    $env:JAVA_HOME = $javaPath
    $env:Path = "$javaBinPath;$env:Path"
    
    Write-Host "JAVA_HOME set to: $javaPath" -ForegroundColor Green
    Write-Host "PATH updated successfully" -ForegroundColor Green
} else {
    Write-Host "Warning: Could not find Java installation path" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[Step 4/4] Verifying installation..." -ForegroundColor Yellow
Write-Host ""

# Refresh environment one more time
refreshenv

# Verify Java version
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host "       Java 17 Installation Completed Successfully!" -ForegroundColor Green
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Java Version:" -ForegroundColor Cyan
    Write-Host $javaVersion -ForegroundColor White
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "1. Close ALL PowerShell/CMD windows" -ForegroundColor White
    Write-Host "2. Open a NEW PowerShell window" -ForegroundColor White
    Write-Host "3. Run: java -version" -ForegroundColor White
    Write-Host "4. Notify the AI assistant to upgrade the project" -ForegroundColor White
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
} else {
    Write-Host "Warning: Java verification failed" -ForegroundColor Yellow
    Write-Host "Please close this window and open a new PowerShell to verify:" -ForegroundColor Yellow
    Write-Host "  java -version" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
