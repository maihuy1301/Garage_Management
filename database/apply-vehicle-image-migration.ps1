param(
    [string]$Server = "localhost,1433",
    [string]$Database = "GarageManagementSystem",
    [string]$Username = "sa",
    [string]$Password = $env:SQLSERVER_PASSWORD
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "YourPassword123"
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$migrationFile = Join-Path $scriptDir "migrations\V02__vehicle_profile_image.sql"

if (-not (Test-Path -LiteralPath $migrationFile)) {
    throw "Migration file not found: $migrationFile"
}

$sqlcmd = Get-Command sqlcmd -ErrorAction SilentlyContinue
if (-not $sqlcmd) {
    throw "sqlcmd was not found. Install SQL Server command line tools before applying the migration."
}

Write-Host "Applying vehicle image migration to '$Database' on $Server..."
& $sqlcmd.Source -S $Server -U $Username -P $Password -C -d $Database -b -i $migrationFile
if ($LASTEXITCODE -ne 0) {
    throw "Vehicle image migration failed."
}

Write-Host "Vehicle image migration completed successfully."
