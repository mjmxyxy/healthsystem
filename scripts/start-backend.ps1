param(
    [string]$DbUrl = "jdbc:mysql://127.0.0.1:3306/healthy_db?useSSL=false&serverTimezone=UTC",
    [string]$DbUser = "healthy",
    [string]$DbPassword = "healthypass",
    [string]$AesKey = "12345678901234567890123456789012"
)

$ErrorActionPreference = "Stop"

Set-Location "$PSScriptRoot/../backend"

if (-not (Test-Path "./pom.xml")) {
    Write-Error "未找到 backend/pom.xml，请确认项目目录。"
}

$javaCmd = Get-Command java -ErrorAction SilentlyContinue
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $javaCmd -or -not $mvnCmd) {
    Write-Error "未检测到 java 或 mvn，请先安装并配置 PATH。"
}

$env:SPRING_DATASOURCE_URL = $DbUrl
$env:SPRING_DATASOURCE_USERNAME = $DbUser
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword
$env:AES_KEY = $AesKey

Write-Host "后端启动中..." -ForegroundColor Cyan
Write-Host "DB: $DbUrl"
Write-Host "User: $DbUser"

mvn spring-boot:run
