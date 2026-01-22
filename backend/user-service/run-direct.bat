@echo off
cd /d "%~dp0"

echo Starting User Service with Spring Boot...

REM Build classpath with all required Spring Boot and dependency jars
set CLASSPATH=target/classes
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot/3.2.0/spring-boot-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-autoconfigure/3.2.0/spring-boot-autoconfigure-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-autoconfigure/3.2.0/spring-boot-autoconfigure-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-context/6.1.1/spring-context-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-web/3.2.0/spring-boot-starter-web-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter/3.2.0/spring-boot-starter-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-web/6.1.1/spring-web-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/spring-webmvc/6.1.1/spring-webmvc-6.1.1.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/fasterxml/jackson/core/jackson-databind/2.15.3/jackson-databind-2.15.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/mybatis/spring/boot/mybatis-spring-boot-starter/3.0.3/mybatis-spring-boot-starter-3.0.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/mybatis/mybatis-spring/3.0.3/mybatis-spring-3.0.3.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/mybatis/mybatis/3.5.13/mybatis-3.5.13.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-data-redis/3.2.0/spring-boot-starter-data-redis-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/data/spring-data-redis/3.2.0/spring-data-redis-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/lettuce/lettuce-core/6.2.6.RELEASE/lettuce-core-6.2.6.RELEASE.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-security/3.2.0/spring-boot-starter-security-3.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/security/spring-security-config/6.2.0/spring-security-config-6.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/security/spring-security-web/6.2.0/spring-security-web-6.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/jsonwebtoken/jjwt-api/0.11.5/jjwt-api-0.11.5.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/jsonwebtoken/jjwt-impl/0.11.5/jjwt-impl-0.11.5.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/io/jsonwebtoken/jjwt-jackson/0.11.5/jjwt-jackson-0.11.5.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springdoc/springdoc-openapi-starter-webmvc-ui/2.2.0/springdoc-openapi-starter-webmvc-ui-2.2.0.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar
set CLASSPATH=%CLASSPATH%;D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-validation/3.2.0/spring-boot-starter-validation-3.2.0.jar

java -cp "%CLASSPATH%" com.dzvote.user.UserApplication --spring.profiles.active=standalone

pause
