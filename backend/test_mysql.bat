@echo off
echo Testing MySQL connection...
docker exec -it dzvote-mysql8 mysql -uroot -pdzvote123 -e "SHOW DATABASES;"
echo.
echo Creating dzvote_v2 database...
docker exec dzvote-mysql8 mysql -uroot -pdzvote123 -e "CREATE DATABASE IF NOT EXISTS dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo.
echo Creating dzvote user...
docker exec dzvote-mysql8 mysql -uroot -pdzvote123 -e "CREATE USER IF NOT EXISTS 'dzvote'@'%' IDENTIFIED BY 'dzvote123';"
echo.
echo Granting privileges...
docker exec dzvote-mysql8 mysql -uroot -pdzvote123 -e "GRANT ALL PRIVILEGES ON dzvote_v2.* TO 'dzvote'@'%'; FLUSH PRIVILEGES;"
echo.
echo Testing dzvote user connection...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; SELECT DATABASE();"
echo.
pause
