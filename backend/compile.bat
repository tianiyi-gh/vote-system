@echo off
cd /d "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend"
javac -encoding UTF-8 -cp "vote-service/target/vote-service-2.0.0.jar;user-service/target/user-service-2.0.0.jar" -d vote-service/target/classes "vote-service/src/main/java/com/dzvote/vote/controller/VoteController.java" "vote-service/src/main/java/com/dzvote/vote/service/impl/VoteServiceImpl.java"
