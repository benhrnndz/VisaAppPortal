# ============================================================
#  Compile & Run — Non-Immigrant VISA Application Portal
#  Updated to include backend packages (model, dao, utils, ui)
# ============================================================

if (-not (Test-Path -Path "bin")) {
    New-Item -ItemType Directory -Path "bin" -Force
}

Write-Host "Compiling Java Swing Visa Application (frontend + backend)..." -ForegroundColor Cyan

# Find ALL Java source files across all packages
$javaFiles = Get-ChildItem -Path "src" -Filter *.java -Recurse | ForEach-Object { $_.FullName }

Write-Host "Found $($javaFiles.Count) source files:" -ForegroundColor Gray
$javaFiles | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }

# Compile with SQLite JDBC in classpath
javac -cp "lib/sqlite-jdbc.jar" -d bin -encoding UTF-8 $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Compilation Successful!" -ForegroundColor Green
    Write-Host "Launching Application..."  -ForegroundColor Cyan
    # Run with SQLite JDBC in classpath (semicolon = Windows path separator)
    java -cp "bin;lib/sqlite-jdbc.jar" com.visa.app.Main
} else {
    Write-Host ""
    Write-Host "Compilation Failed (exit code $LASTEXITCODE)." -ForegroundColor Red
    Write-Host "Check the error messages above." -ForegroundColor Yellow
}
