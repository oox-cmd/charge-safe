@echo off
setlocal
if not defined JWT_SECRET (
  echo 请先设置 JWT_SECRET 后再启动后端。
  exit /b 1
)
if not defined CHARGE_SAFE_DB_USERNAME (
  echo 请先设置 CHARGE_SAFE_DB_USERNAME 后再启动后端。
  exit /b 1
)
if not defined CHARGE_SAFE_DB_PASSWORD (
  echo 请先设置 CHARGE_SAFE_DB_PASSWORD 后再启动后端。
  exit /b 1
)
cd /d "%~dp0charge-safe-backend"
mvn spring-boot:run -Dspring-boot.run.profiles=local
