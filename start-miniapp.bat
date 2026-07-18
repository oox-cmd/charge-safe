@echo off
setlocal
cd /d "%~dp0charge-safe-miniapp"
call npm run dev:h5
