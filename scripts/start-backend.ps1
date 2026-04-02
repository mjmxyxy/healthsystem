param(
    [string]$DbUrl = "jdbc:mysql://127.0.0.1:3306/healthy_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
    [string]$DbUser = "healthy",
    [string]$DbPassword = "healthypass",
    [string]$AesKey = "12345678901234567890123456789012",
    [Parameter(Mandatory = $true)]
    [string]$AiApiKey,
    [string]$AiApiUrl = "https://ark.cn-beijing.volces.com/api/v3",
    [string]$AiModel = "ep-20260402155750-glf24",
    [int]$AiTimeoutSeconds = 30
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

$env:AI_API_KEY = $AiApiKey
$env:AI_API_URL = $AiApiUrl
$env:AI_MODEL = $AiModel
$env:AI_TIMEOUT_SECONDS = $AiTimeoutSeconds

Write-Host "后端启动中..." -ForegroundColor Cyan
Write-Host "DB: $DbUrl"
Write-Host "User: $DbUser"
Write-Host "AI: enabled ($AiModel)"

mvn spring-boot:run
