@echo off
REM ============================================================
REM ChargeSafe 后端本地启动脚本 (Windows)
REM 使用前请确认：
REM   1. MySQL 已启动，且已建库 charge_safe_dev / 用户 charge_safe_app
REM   2. JAVA_HOME 指向 JDK 21（默认路径为本机常见安装位置，若不同请修改下方 SET JAVA_HOME）
REM   3. 8080 端口未被占用（被占用可改下方 PORT 变量）
REM ============================================================

setlocal enabledelayedexpansion

REM === 可按需修改的配置 ===
set "PORT=8080"
set "PROFILE=local"
set "MAVEN_PATH=C:\tools\apache-maven-3.9.8\bin\mvn.cmd"

REM === JDK 路径（按实际安装位置修改，常见路径已列出，取消注释即可） ===
set "JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"

REM === 校验 JAVA_HOME ===
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JAVA_HOME 不存在或不是有效 JDK: %JAVA_HOME%
    echo 请用记事本打开本脚本，修改 JAVA_HOME 为你机器上 JDK 21 的实际安装路径。
    pause
    exit /b 1
)

REM === 校验 Maven ===
if not exist "%MAVEN_PATH%" (
    echo [ERROR] Maven 不存在: %MAVEN_PATH%
    echo 请修改脚本顶部 MAVEN_PATH 变量。
    pause
    exit /b 1
)

REM === 设置环境变量 ===
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN_OPTS=-Xmx1024m"

echo ============================================================
echo ChargeSafe 后端启动中...
echo JAVA_HOME   = %JAVA_HOME%
echo Maven       = %MAVEN_PATH%
echo Profile     = %PROFILE%
echo Port        = %PORT%
echo ============================================================

REM === 切换到脚本所在目录（即后端模块根目录） ===
cd /d "%~dp0"

REM === 启动 ===
"%MAVEN_PATH%" spring-boot:run -Dspring-boot.run.profiles=%PROFILE% -Dspring-boot.run.arguments=--server.port=%PORT%

REM === 如果进程结束，保留窗口查看错误 ===
echo.
echo 进程已结束。按任意键退出...
pause
