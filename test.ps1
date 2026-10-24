$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force -Path "bin" | Out-Null

$sourceFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -d "bin" $sourceFiles

if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

$tests = Get-ChildItem -Path "test" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
$console = Join-Path $PSScriptRoot "lib\junit-platform-console-standalone.jar"

if (-not (Test-Path $console)) {
    Write-Warning "JUnit console launcher not found at $console. Sources compiled, but tests were not executed."
    exit 0
}

javac -cp "bin;$console" -d "bin" $tests
java -jar $console execute --class-path "bin" --scan-class-path
