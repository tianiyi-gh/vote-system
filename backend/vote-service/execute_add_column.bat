@echo off
echo Adding columns to activities table...
mysql -h localhost -P 3307 -u root -pmysql8123 --ssl-mode=DISABLED dzvote_v2 < add_cover_image_column.sql
if %errorlevel% equ 0 (
    echo Columns added successfully!
) else (
    echo Failed to add columns. Please check error messages.
)
pause

