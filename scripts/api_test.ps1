# Wait for backend
while(-not (Test-NetConnection -ComputerName 'localhost' -Port 8082).TcpTestSucceeded) { Write-Host 'waiting backend...'; Start-Sleep -Seconds 1 }
Write-Host 'backend up'

Write-Host '--- scales ---'
$s = Invoke-RestMethod -Uri 'http://localhost:8082/api/scale' -Method Get
$s | ConvertTo-Json

Write-Host '--- questions ---'
$qs = Invoke-RestMethod -Uri 'http://localhost:8082/api/scale/1/questions?page=1&pageSize=15' -Method Get
$qs | ConvertTo-Json

Write-Host '--- submit result ---'
$answers = 1..15 | ForEach-Object { @{ questionId = $_; answer = 2 } }
$body = @{ answers = $answers } | ConvertTo-Json
$res = Invoke-RestMethod -Uri 'http://localhost:8082/api/scale/1/submit' -Method Post -Body $body -ContentType 'application/json' -ErrorAction Stop
$res | ConvertTo-Json

Write-Host '--- reports list ---'
Invoke-RestMethod -Uri 'http://localhost:8082/api/scale/report' -Method Get | ConvertTo-Json
