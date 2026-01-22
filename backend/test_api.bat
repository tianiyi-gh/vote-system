@echo off
chcp 65001 >nul
echo ====================================
echo 测试 User Service - 登录接口
echo ====================================
echo.

echo 1. 测试登录接口
curl -X POST http://localhost:8084/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
echo.

echo.
echo ====================================
echo 测试 Vote Service - 投票接口
echo ====================================
echo.

echo 2. 测试投票接口
curl -X POST http://localhost:8082/api/votes ^
  -H "Content-Type: application/json" ^
  -d "{\"activityId\":1,\"candidateId\":1,\"channel\":\"WEB\",\"voterPhone\":\"13800138000\",\"captcha\":\"1234\",\"captchaKey\":\"test\"}"
echo.

echo.
echo 3. 测试健康检查
curl http://localhost:8084/api/auth/health
echo.

curl http://localhost:8082/api/votes/health
echo.

pause
