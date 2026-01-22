# Force Install Java 17 to Custom Directory
# This script will forcefully install Java 17 to D:\ide\Java\java17

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  Force Install Java 17 to D:\ide\Java\java17" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Check Administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: Please run as Administrator!" -ForegroundColor Red
    exit 1
}

# Create installation directory
$installDir = "D:\ide\Java\java17"
Write-Host "Target directory: $installDir" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path "D:\ide\Java")) {
    Write-Host "Creating D:\ide\Java directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "D:\ide\Java" -Force | Out-Null
    Write-Host "  Created!" -ForegroundColor Green
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "Step 1: Uninstalling old version (if exists)..." -ForegroundColor Yellow
Write-Host "================================================================" -ForegroundColor Cyan

# Uninstall old version
choco uninstall temurin17 -y --all-versions

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "Step 2: Installing Java 17 to $installDir..." -ForegroundColor Yellow
Write-Host "This may take 5-10 minutes, please wait..." -ForegroundColor Gray
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Install with custom parameters
$chocoArgs = @(
    "install",
    "temurin17",
    "-y",
    "--force",
    "--params='/INSTALLDIR=""$installDir"" /ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /ALLUSERS=1'"
)

& choco @chocoArgs

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "Step 3: Verifying installation..." -ForegroundColor Yellow
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Check installation
if (Test-Path $installDir) {
    $javaExe = Join-Path $installDir "bin\java.exe"
    
    if (Test-Path $javaExe) {
        Write-Host "Installation successful!" -ForegroundColor Green
        Write-Host "Location: $installDir" -ForegroundColor White
        Write-Host ""
        
        Write-Host "Java Version:" -ForegroundColor Cyan
        & $javaExe -version 2>&1
        Write-Host ""
        
        # Configure environment variables
        Write-Host "================================================================" -ForegroundColor Cyan
        Write-Host "Step 4: Configuring environment variables..." -ForegroundColor Yellow
        Write-Host "================================================================" -ForegroundColor Cyan
        Write-Host ""
        
        # Set JAVA_HOME
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $installDir, [System.EnvironmentVariableTarget]::Machine)
        Write-Host "  JAVA_HOME = $installDir" -ForegroundColor Green
        
        # Update PATH
        $machinePath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
        $javaBinPath = Join-Path $installDir "bin"
        
        # Remove old Java paths and add new one at the beginning
        $pathParts = $machinePath -split ";" | Where-Object { $_ -and $_ -notlike "*jdk1.8*" }
        
        if ($pathParts -notcontains $javaBinPath) {
            $newPath = $javaBinPath + ";" + ($pathParts -join ";")
            [System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)
            Write-Host "  PATH updated" -ForegroundColor Green
        }
        
        # Set for current session
        $env:JAVA_HOME = $installDir
        $env:Path = "$javaBinPath;" + $env:Path
        
        Write-Host ""
        Write-Host "================================================================" -ForegroundColor Cyan
        Write-Host "  Java 17 Installation Complete!" -ForegroundColor Green
        Write-Host "================================================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Installation Details:" -ForegroundColor Cyan
        Write-Host "  Location: $installDir" -ForegroundColor White
        Write-Host "  JAVA_HOME: $env:JAVA_HOME" -ForegroundColor White
        Write-Host ""
        Write-Host "Next Steps:" -ForegroundColor Yellow
        Write-Host "  1. Close ALL PowerShell/CMD windows" -ForegroundColor White
        Write-Host "  2. Open a NEW PowerShell window" -ForegroundColor White
        Write-Host "  3. Run: java -version" -ForegroundColor Cyan
        Write-Host "  4. Should see: openjdk version ""17.0.x""" -ForegroundColor White
        Write-Host "  5. Notify the AI to upgrade the project" -ForegroundColor White
        Write-Host ""
        Write-Host "================================================================" -ForegroundColor Cyan
        
    } else {
        Write-Host "ERROR: Installation failed - java.exe not found!" -ForegroundColor Red
        Write-Host "Expected location: $javaExe" -ForegroundColor Yellow
    }
} else {
    Write-Host "ERROR: Installation directory not found!" -ForegroundColor Red
    Write-Host "Expected location: $installDir" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Chocolatey may have installed to a different location." -ForegroundColor Yellow
    Write-Host "Let me search for it..." -ForegroundColor Yellow
    Write-Host ""
    
    # Search in common locations
    $searchPaths = @(
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Temurin", 
        "C:\Program Files\Java",
        "C:\Program Files (x86)\Eclipse Adoptium"
    )
    
    foreach ($path in $searchPaths) {
        if (Test-Path $path) {
            Write-Host "Checking: $path" -ForegroundColor Gray
            Get-ChildItem $path -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*jdk-17*" -or $_.Name -like "*17.*" } | ForEach-Object {
                Write-Host "  Found: $($_.FullName)" -ForegroundColor Green
            }
        }
    }
}

Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
