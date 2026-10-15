$env:JAVA_HOME = "C:\Users\ASUS\tools\jdk-21.0.6+7"
$env:Path = "$env:JAVA_HOME\bin;C:\Users\ASUS\tools\apache-maven-3.9.9\bin;" + $env:Path

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " EventFlow Online Event Management System (Spring Boot 3) " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Port: http://localhost:8080" -ForegroundColor Yellow
Write-Host "Admin:     admin@eventflow.com     | Admin@123"
Write-Host "Organizer: organizer@eventflow.com | Organizer@123"
Write-Host "Attendee:  attendee@eventflow.com  | Attendee@123"
Write-Host "----------------------------------------------------------"

mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
