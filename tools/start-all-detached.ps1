# Detached startup: server + mindcraft bots, parented to WmiPrvSE so they
# survive the launching session/process ending.
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\start-all-detached.ps1
#
# Edit the constants below to match your installation.

$Base = 'C:\PCL-main'                    # workspace root (server/ and _tools/mindcraft live here)
$Java = 'C:\MCAI\jdk-21.0.12.1+1\bin\java.exe'
$Node = 'C:\Program Files\nodejs\node.exe'
$ServerPort = 25566
$MindserverPort = 8080

# --- Zombie cleanup ----------------------------------------------------------
# Graceful `stop` closes the port, but some modpack JVMs never exit
# (non-daemon AI-mod threads keep them alive) while still holding jar locks.
# Kill any server JVM that no longer owns a listening socket.
$prev = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'fabric-server-launch' }
foreach ($z in $prev) {
    $listening = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
        Where-Object OwningProcess -eq $z.ProcessId
    if (-not $listening) {
        Stop-Process -Id $z.ProcessId -Force -ErrorAction SilentlyContinue
        Write-Host ('[CLEANUP] Killed zombie server JVM pid=' + $z.ProcessId)
    }
}

# --- Server -------------------------------------------------------------------
if (Get-NetTCPConnection -LocalPort $ServerPort -State Listen -ErrorAction SilentlyContinue) {
    Write-Host '[WARN] Server port already listening, skipping server start.'
} else {
    $srv = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine      = 'cmd /c start_server.bat'
        CurrentDirectory = "$Base\server"
    }
    Write-Host ('[OK] Server launching, pid={0} rc={1}' -f $srv.ProcessId, $srv.ReturnValue)
}

# --- Wait for server port -----------------------------------------------------
$up = $false
foreach ($i in 1..60) {
    Start-Sleep -Seconds 3
    if (Get-NetTCPConnection -LocalPort $ServerPort -State Listen -ErrorAction SilentlyContinue) { $up = $true; break }
}
if (-not $up) { Write-Host '[FAIL] Server port not listening after 180s.'; exit 1 }
Write-Host '[OK] Server is listening.'

# --- Mindcraft bots -----------------------------------------------------------
if (Get-NetTCPConnection -LocalPort $MindserverPort -State Listen -ErrorAction SilentlyContinue) {
    Write-Host '[WARN] Mindserver port already listening, skipping mindcraft start.'
} else {
    $mc = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine      = 'cmd /c start_mindcraft.bat'
        CurrentDirectory = "$Base\_tools\mindcraft"
    }
    Write-Host ('[OK] mindcraft launching, pid={0} rc={1}' -f $mc.ProcessId, $mc.ReturnValue)
}

# --- Ollama shim (AI-Player dependency) ---------------------------------------
# AI-Player hardwires its intent/RAG calls to a local Ollama on 11434; the shim
# translates them to the configured OpenAI-compatible API and fakes embeddings.
# Without it, AI-Player conversations die with an "NLP issue" chat message.
$ShimPort = 11434
if (Get-NetTCPConnection -LocalPort $ShimPort -State Listen -ErrorAction SilentlyContinue) {
    Write-Host '[WARN] Shim port already listening, skipping ollama-shim start.'
} else {
    $shim = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine      = "$Node `"$Base\_tools\ollama-shim.mjs`""
        CurrentDirectory = "$Base\_tools"
    }
    Write-Host ('[OK] ollama-shim launching, pid={0} rc={1}' -f $shim.ProcessId, $shim.ReturnValue)
}
