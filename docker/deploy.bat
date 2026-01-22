@echo off
echo 开始构建DZVote系统...

REM 构建后端服务
echo 构建后端服务...
cd ..\backend

REM 构建各个微服务
echo 构建 activity-service...
cd activity-service
call mvn clean package -DskipTests
cd ..

echo 构建 vote-service...
cd vote-service
call mvn clean package -DskipTests
cd ..

echo 构建 statistics-service...
cd statistics-service
call mvn clean package -DskipTests
cd ..

echo 构建 gateway-service...
cd gateway-service
call mvn clean package -DskipTests
cd ..

cd ..\..

REM 构建前端
echo 构建前端管理后台...
cd frontend\admin
call npm install
call npm run build
cd ..

echo 构建H5移动端...
cd h5
call npm install
call npm run build
cd ..\..

REM 启动Docker容器
echo 启动Docker容器...
cd docker
docker-compose up -d

echo 构建完成！
echo 管理后台地址: http://localhost
echo H5移动端地址: http://localhost:81
echo API网关地址: http://localhost:8084
pause