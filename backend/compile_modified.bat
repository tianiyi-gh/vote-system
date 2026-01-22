@echo off
cd /d "%~dp0"

echo 编译修改的类文件...
set CLASSPATH=target\vote-service-2.0.0.jar

cd vote-service\target\classes
javac -encoding UTF-8 -cp "..\..\..\%CLASSPATH%;..\..\..\user-service\target\user-service-2.0.0.jar" ^
    "..\..\src\main\java\com\dzvote\vote\controller\VoteController.java" ^
    "..\..\src\main\java\com\dzvote\vote\service\impl\VoteServiceImpl.java"

if errorlevel 1 (
    echo 编译失败!
    pause
    exit /b 1
)

echo 编译成功!
echo.
echo 现在运行 create_jar_v2.py 重新打包 jar
pause
