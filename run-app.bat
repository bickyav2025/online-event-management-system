@echo off
set "JAVA_HOME=C:\Users\ASUS\tools\jdk-21.0.6+7"
set "PATH=%JAVA_HOME%\bin;C:\Users\ASUS\tools\apache-maven-3.9.9\bin;%PATH%"

echo Starting EventFlow Online Event Management System on http://localhost:8080 ...
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
pause
