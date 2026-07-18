@echo off
REM ChargeSafe local backend launcher for Windows.
REM Uses JDK 21 and Maven found on PATH. Do not put secrets in this file.

setlocal
cd /d "%~dp0"

set "PORT=8080"
set "PROFILE=local"

if "%CHARGE_SAFE_DB_USERNAME%"=="" (
    echo [ERROR] CHARGE_SAFE_DB_USERNAME is not set.
    goto :error
)
if "%CHARGE_SAFE_DB_PASSWORD%"=="" (
    echo [ERROR] CHARGE_SAFE_DB_PASSWORD is not set.
    goto :error
)
if "%JWT_SECRET%"=="" (
    echo [ERROR] JWT_SECRET is not set.
    goto :error
)

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] java was not found on PATH. Configure JDK 21 and try again.
    goto :error
)
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] mvn was not found on PATH. Configure Maven and try again.
    goto :error
)

echo ============================================================
echo ChargeSafe backend starting...
echo Profile     = %PROFILE%
echo Port        = %PORT%
echo ============================================================
java -version
mvn -version

mvn spring-boot:run -Dspring-boot.run.profiles=%PROFILE% -Dspring-boot.run.arguments=--server.port=%PORT%
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo.
    echo [ERROR] Backend process stopped with exit code: %EXIT_CODE%
)
exit /b %EXIT_CODE%

:error
echo.
echo Set database credentials and JWT_SECRET in the current shell first.
echo Confirm that java -version and mvn -version use JDK 21.
exit /b 1
