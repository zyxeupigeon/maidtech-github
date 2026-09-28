@echo off
title Stop Mindcraft
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name = 'node.exe'\" | Where-Object { \.CommandLine -match 'mindcraft' } | ForEach-Object { Stop-Process -Id \.ProcessId -Force -ErrorAction SilentlyContinue }"
echo Mindcraft stopped. (or kick builder_2 in game)
pause