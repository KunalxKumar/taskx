$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force -Path "bin" | Out-Null
$sourceFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

if ($sourceFiles.Count -eq 0) {
    throw "No Java source files found."
}

javac -d "bin" $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

Write-Host "Build completed successfully." -ForegroundColor Green
