$ErrorActionPreference = "Stop"

if (-not (Test-Path "bin")) {
    & "$PSScriptRoot\build.ps1"
}

$classpath = "bin"
$mysql = Join-Path $PSScriptRoot "lib\mysql-connector-j-9.7.0.jar"

if (Test-Path $mysql) {
    $classpath = "$classpath;$mysql"
}

java -cp $classpath Main
