@echo off
cd /d "D:\system\Desktop\charge-safe-main(4)\charge-safe-miniapp"
set "NODE_OPTIONS=--openssl-legacy-provider"
call npm run dev:h5
