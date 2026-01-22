@echo off
cd /d "%~dp0"
echo Building vote-service...

REM Clean target directory
if exist target (
    rmdir /s /q target
)

REM Create target directories
mkdir target\classes

REM Build classpath
set CLASSPATH=D:/ide/maven3.9/repository/org/springframework/boot/spring-boot/3.2.0/spring-boot-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-autoconfigure/3.2.0/spring-boot-autoconfigure-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-web/3.2.0/spring-boot-starter-web-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-web/6.1.1/spring-web-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-context/6.1.1/spring-context-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-core/6.1.1/spring-core-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-beans/6.1.1/spring-beans-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-webmvc/6.1.1/spring-webmvc-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-aop/6.1.1/spring-aop-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-expression/6.1.1/spring-expression-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-jcl/6.1.1/spring-jcl-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/fasterxml/jackson/core/jackson-databind/2.15.3/jackson-databind-2.15.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/fasterxml/jackson/core/jackson-core/2.15.3/jackson-core-2.15.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/fasterxml/jackson/core/jackson-annotations/2.15.3/jackson-annotations-2.15.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/mybatis/spring/boot/mybatis-spring-boot-starter/3.0.3/mybatis-spring-boot-starter-3.0.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/mybatis/mybatis-spring/3.0.3/mybatis-spring-3.0.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/mybatis/mybatis/3.5.13/mybatis-3.5.13.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-data-redis/3.2.0/spring-boot-starter-data-redis-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/data/spring-data-redis/3.2.0/spring-data-redis-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-tx/6.1.1/spring-tx-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-oxm/6.1.1/spring-oxm-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/lettuce/lettuce-core/6.2.6.RELEASE/lettuce-core-6.2.6.RELEASE.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/netty/netty-common/4.1.100.Final/netty-common-4.1.100.Final.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/netty/netty-handler/4.1.100.Final/netty-handler-4.1.100.Final.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/netty/netty-resolver/4.1.100.Final/netty-resolver-4.1.100.Final.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/netty/netty-buffer/4.1.100.Final/netty-buffer-4.1.100.Final.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/netty/netty-codec/4.1.100.Final/netty-codec-4.1.100.Final.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/netty/netty-transport/4.1.100.Final/netty-transport-4.1.100.Final.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-validation/3.2.0/spring-boot-starter-validation-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springdoc/springdoc-openapi-starter-webmvc-ui/2.2.0/springdoc-openapi-starter-webmvc-ui-2.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/apache/poi/poi-ooxml/5.2.3/poi-ooxml-5.2.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/apache/poi/poi-ooxml-lite/5.2.3/poi-ooxml-lite-5.2.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/apache/poi/poi/5.2.3/poi-5.2.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/commons-codec/commons-codec/1.15/commons-codec-1.15.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar

REM Compile sources
echo Compiling Java sources...
javac -encoding UTF-8 -d target\classes -cp "%CLASSPATH%;target\classes" ^
    -processorpath "D:/ide/maven3.9/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar" ^
    src\main\java\com\dzvote\vote\*.java ^
    src\main\java\com\dzvote\vote\config\*.java ^
    src\main\java\com\dzvote\vote\controller\*.java ^
    src\main\java\com\dzvote\vote\dto\*.java ^
    src\main\java\com\dzvote\vote\entity\*.java ^
    src\main\java\com\dzvote\vote\mapper\*.java ^
    src\main\java\com\dzvote\vote\service\*.java ^
    src\main\java\com\dzvote\vote\service\impl\*.java ^
    src\main\java\com\dzvote\vote\util\*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Copy resources
echo Copying resources...
xcopy /y /e src\main\resources\* target\classes\

REM Run Python script to create jar
echo Creating JAR file...
D:/ide/python/Python313/python.exe create_jar.py

echo Build complete!
pause
