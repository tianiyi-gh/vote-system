@echo off
cd /d "%~dp0"
echo Building user-service...

REM Clean target directory
if exist target (
    rmdir /s /q target
)

REM Create target directory
mkdir target

REM Compile sources
javac -encoding UTF-8 -d target/classes -cp "D:/ide/maven3.9/repository/org/springframework/boot/spring-boot/3.2.0/spring-boot-3.2.0.jar;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-autoconfigure/3.2.0/spring-boot-autoconfigure-3.2.0.jar" ^
    src/main/java/com/dzvote/user/UserApplication.java ^
    src/main/java/com/dzvote/user/config/*.java ^
    src/main/java/com/dzvote/user/controller/*.java ^
    src/main/java/com/dzvote/user/dto/*.java ^
    src/main/java/com/dzvote/user/entity/*.java ^
    src/main/java/com/dzvote/user/mapper/*.java ^
    src/main/java/com/dzvote/user/service/*.java ^
    src/main/java/com/dzvote/user/service/impl/*.java ^
    src/main/java/com/dzvote/user/util/*.java

REM Copy resources
xcopy /y /e src\main\resources\* target\classes\

echo Build complete!
pause
