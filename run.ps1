# Compile and Run VISA App
# Run from VISA_App root: .\run.ps1

$BIN = "bin"
$LIB = "frontend\lib\sqlite-jdbc.jar"

if (-not (Test-Path $BIN)) {
    New-Item -ItemType Directory -Path $BIN -Force | Out-Null
}

Write-Host "Collecting source files..." -ForegroundColor Cyan

$frontendFiles = Get-ChildItem -Path "frontend\src" -Filter *.java -Recurse | ForEach-Object { $_.FullName }
$backendFiles  = Get-ChildItem -Path "backend\src"  -Filter *.java -Recurse | ForEach-Object { $_.FullName }
$allFiles = $frontendFiles + $backendFiles

Write-Host "Frontend: $($frontendFiles.Count) files" -ForegroundColor Gray
Write-Host "Backend:  $($backendFiles.Count) files"  -ForegroundColor Gray
Write-Host "Total:    $($allFiles.Count) files"       -ForegroundColor White
Write-Host "Compiling..." -ForegroundColor Cyan

javac -cp $LIB -d $BIN -encoding UTF-8 $allFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation Successful!" -ForegroundColor Green
    Write-Host "Launching..." -ForegroundColor Cyan
    java -cp "$BIN;$LIB" com.visa.app.Main
} else {
    Write-Host "Compilation Failed." -ForegroundColor Red
}
