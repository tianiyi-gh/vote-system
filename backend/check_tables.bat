@echo off
echo Checking tables in dzvote_v2...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; SHOW TABLES;"
echo.
echo User table structure...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; DESC user;" 2>nul || echo Table 'user' does not exist
echo.
echo Vote record table structure...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; DESC vote_record;" 2>nul || echo Table 'vote_record' does not exist
echo.
echo Vote limit table structure...
docker exec dzvote-mysql8 mysql -udzvote -pdzvote123 -e "USE dzvote_v2; DESC vote_limit;" 2>nul || echo Table 'vote_limit' does not exist
echo.
pause
