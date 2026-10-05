# ============================================================
#  VisaAppPortal Launch Script (Windows PowerShell)
# ============================================================
#
# Usage:
#   .\run.ps1          - Launches using Gradle Wrapper (default)
#   .\run.ps1 -Maven   - Launches using Apache Maven
#   .\run.ps1 -Direct  - Compiles and runs directly with javac/java
#
param(
    [switch]$Maven,
    [switch]$Direct
)

$ErrorActionPreference = "Stop"
Write-Host "=== VisaAppPortal Launch ===" -ForegroundColor Cyan

if ($Maven) {
    Write-Host "Building and launching with Maven..." -ForegroundColor Yellow
    mvn compile exec:java
    exit $LASTEXITCODE
}

if ($Direct) {
    Write-Host "Compiling directly with javac..." -ForegroundColor Yellow
    $BIN = "bin"
    $LIB = "lib\sqlite-jdbc.jar"
    if (-not (Test-Path $BIN)) {
        New-Item -ItemType Directory -Path $BIN -Force | Out-Null
    }
    $javaFiles = Get-ChildItem -Path "src\main\java" -Filter *.java -Recurse | ForEach-Object { $_.FullName }
    javac -cp $LIB -d $BIN -encoding UTF-8 $javaFiles
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation successful! Starting UI..." -ForegroundColor Green
        java -cp "$BIN;$LIB" com.visa.app.Main
    }
    exit $LASTEXITCODE
}

# Default: Gradle Wrapper
if (Test-Path ".\gradlew.bat") {
    Write-Host "Building and launching with Gradle Wrapper..." -ForegroundColor Green
    .\gradlew.bat run
} else {
    Write-Host "Gradle wrapper not found, falling back to Maven..." -ForegroundColor Yellow
    mvn compile exec:java
}
