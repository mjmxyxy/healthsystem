param(
    [string]$MySqlHost = "127.0.0.1",
    [int]$MySqlPort = 3306,
    [string]$MySqlUser = "root",
    [string]$SchemaFile = "../sql/schema.sql"
)

$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

if (-not (Test-Path $SchemaFile)) {
    Write-Error "未找到 schema 文件: $SchemaFile"
}

$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlCmd) {
    Write-Error "未检测到 mysql 客户端，请先安装 MySQL Client 并加入 PATH。"
}

$pwd = Read-Host "请输入 MySQL 密码（用于用户 $MySqlUser）"
if ([string]::IsNullOrWhiteSpace($pwd)) {
    Write-Error "密码不能为空。"
}

Write-Host "开始导入数据库结构..." -ForegroundColor Cyan
Get-Content $SchemaFile | & mysql -h $MySqlHost -P $MySqlPort -u $MySqlUser "--password=$pwd"

if ($LASTEXITCODE -ne 0) {
    Write-Error "导入失败，请检查 MySQL 账号密码和服务状态。"
}

Write-Host "导入完成，开始验证表..." -ForegroundColor Cyan
& mysql -h $MySqlHost -P $MySqlPort -u $MySqlUser "--password=$pwd" -e "USE healthy_db; SHOW TABLES;"

if ($LASTEXITCODE -ne 0) {
    Write-Error "验证失败，请手工检查数据库 healthy_db。"
}

Write-Host "数据库初始化完成。" -ForegroundColor Green
