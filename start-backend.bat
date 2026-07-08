@echo off
set "JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "D:\system\Desktop\charge-safe-main(4)\charge-safe-backend"
"C:\tools\apache-maven-3.9.8\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments=--server.port=8080
