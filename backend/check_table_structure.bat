@echo off
echo Checking vote_records structure...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; DESC vote_records;"
echo.
echo Checking vote_limits structure...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; DESC vote_limits;"
pause
