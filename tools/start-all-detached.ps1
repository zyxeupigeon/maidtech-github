# Start Minecraft server + mindcraft via WMI (parent = WmiPrvSE, so they
# survive the calling session/process ending).
# Launches the ASCII .bat launchers so the layout matches the desktop
# buttons: exactly 2 console windows (server + mindcraft bot).
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File C:\PCL-main\_tools\start-all-detached.ps1

$java = 'C:\MCAI\jdk-21.0.12.1+1\bin\java.exe'
$node = 'C:\Program Files\nodejs\node.exe'

# Kill zombie server JVMs: graceful `stop` closes the port but this modpack's
# JVM sometimes never exits (non-daemon AI-mod threads), holding jar locks.
$prev = Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -match 'fabric-server-launch' }
foreach ($z in $prev) {
    $listening = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object OwningProcess -eq $z.ProcessId
    if (-not $listening) {
        Stop-Process -Id $z.ProcessId -Force -ErrorAction SilentlyContinue
        Write-Host ('[CLEANUP] Killed zombie server JVM pid=' + $z.ProcessId)
    }
}

if (Get-NetTCPConnection -LocalPort 25566 -State Listen -ErrorAction SilentlyContinue) {
    Write-Host '[WARN] Port 25566 already listening, server probably running. Skip server start.'
} else {
    $srv = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine      = 'C:\PCL-main\server\start-server.bat'
        CurrentDirectory = 'C:\PCL-main\server'
    }
    Write-Host ('[OK] Server launching, pid={0} rc={1}' -f $srv.ProcessId, $srv.ReturnValue)
}

# Wait for server port (up to 3 min)
$up = $false
foreach ($i in 1..60) {
    Start-Sleep -Seconds 3
    if (Get-NetTCPConnection -LocalPort 25566 -State Listen -ErrorAction SilentlyContinue) { $up = $true; break }
}
if (-not $up) { Write-Host '[FAIL] Port 25566 not listening after 180s.'; exit 1 }
Write-Host '[OK] Server is listening on 25566.'

# Mindcraft is DISABLED by default (user decision 2026-09-30: save tokens).
# To start it manually: powershell -File C:\PCL-main\_tools\mindcraft\start-mindcraft.bat
# (botwatch is also paused via C:\PCL-main\_tools\botwatch_off.txt)
$StartMindcraft = $false
if ($StartMindcraft) {
    if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
        Write-Host '[WARN] Port 8080 already listening, mindcraft probably running. Skip mindcraft start.'
    } else {
        $mc = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
            CommandLine      = 'C:\PCL-main\_tools\mindcraft\start-mindcraft.bat'
            CurrentDirectory = 'C:\PCL-main\_tools\mindcraft'
        }
        Write-Host ('[OK] mindcraft launching, pid={0} rc={1}' -f $mc.ProcessId, $mc.ReturnValue)
    }
} else {
    Write-Host '[INFO] mindcraft disabled by default - not starting.'
}

