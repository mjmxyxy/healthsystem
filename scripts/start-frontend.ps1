param(
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"

Set-Location "$PSScriptRoot/../frontend"

if (-not (Test-Path "./package.json")) {
    Write-Error "未找到 frontend/package.json，请确认项目目录。"
}

$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
$npmCmd = Get-Command npm -ErrorAction SilentlyContinue
if (-not $nodeCmd -or -not $npmCmd) {
    Write-Error "未检测到 node 或 npm，请先安装并配置 PATH。"
}

if (-not $SkipInstall) {
    Write-Host "安装前端依赖..." -ForegroundColor Cyan
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Error "npm install 失败。"
    }
}

Write-Host "前端启动中..." -ForegroundColor Cyan
npm run dev
