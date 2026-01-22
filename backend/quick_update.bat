@echo off
cd /d "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend"

echo 编译修改的类文件...

set CLASSPATH=vote-service/target/vote-service-2.0.0.jar;user-service/target/user-service-2.0.0.jar;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot/3.2.0/spring-boot-3.2.0.jar;D:/ide/maven3.9/repository/org/springframework/spring-web/6.1.1/spring-web-6.1.1.jar;D:/ide/maven3.9/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar;D:/ide/maven3.9/repository/com/fasterxml/jackson/core/jackson-databind/2.15.3/jackson-databind-2.15.3.jar;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-web/3.2.0/spring-boot-starter-web-3.2.0.jar

javac -encoding UTF-8 -cp "%CLASSPATH%" -d vote-service/target/classes vote-service/src/main/java/com/dzvote/vote/controller/VoteController.java vote-service/src/main/java/com/dzvote/vote/service/impl/VoteServiceImpl.java

if errorlevel 1 (
    echo 编译失败!
    pause
    exit /b 1
)

echo 编译成功!

echo 更新 jar 文件...
cd vote-service/target/classes
jar uf vote-service-2.0.0.jar com/dzvote/vote/controller/VoteController.class com/dzvote/vote/service/impl/VoteServiceImpl.class

if errorlevel 1 (
    echo 更新 jar 失败!
) else (
    echo jar 文件更新成功!
)

cd /d "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend"
pause
