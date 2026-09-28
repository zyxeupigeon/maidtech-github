@echo off
title Mindcraft builder_2 - close window to stop
cd /d "%~dp0"

echo ============================================
echo   Mindcraft AI bot (builder_2)
echo   Server must be running first (port 25566)
echo   Close this window to stop the bot.
echo ============================================

rem --- refuse to start if mindserver port 8080 already taken ---
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] Port 8080 already in use - mindcraft is probably already running.
    echo        This window will close in 5 seconds.
    timeout /t 5 /nobreak >nul
    exit /b 0
)

rem --- stop any leftover mindcraft node processes ---
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name = 'node.exe'\" | Where-Object { $_.CommandLine -match 'main\.js' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"
timeout /t 2 /nobreak >nul

echo Starting mindcraft...
node main.js
echo.
echo [Mindcraft stopped]
pause