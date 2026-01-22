@echo off
echo Generating service_id for existing activities...
mysql -h localhost -P 3307 -u root -pmysql8123 --ssl-mode=DISABLED dzvote_v2 < generate_service_id.sql
if %errorlevel% equ 0 (
    echo Service IDs generated successfully!
) else (
    echo Failed to generate service IDs. Please check error messages.
)
pause

