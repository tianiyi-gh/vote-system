@echo off
cd /d d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
echo Starting vote-service on port 8082...
java -jar target\vote-service-2.0.0.jar --spring.profiles.active=standalone --server.port=8082
pause
