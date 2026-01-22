@echo off
chcp 65001 >nul
echo ===================================
echo Executing database fix scripts
echo ===================================
echo.

echo [1/3] Adding new columns to activities table...
mysql -h localhost -P 3307 -u root -pmysql8123 --ssl-mode=DISABLED dzvote_v2 < add_cover_image_column.sql
if %errorlevel% equ 0 (
    echo [OK] Activities columns added successfully
) else (
    echo [ERROR] Failed to add activities columns
)
echo.

echo [2/3] Adding new columns to candidates table...
mysql -h localhost -P 3307 -u root -pmysql8123 --ssl-mode=DISABLED dzvote_v2 < add_candidates_columns.sql
if %errorlevel% equ 0 (
    echo [OK] Candidates columns added successfully
) else (
    echo [ERROR] Failed to add candidates columns
)
echo.

echo [3/3] Generating serviceId for existing activities...
mysql -h localhost -P 3307 -u root -pmysql8123 --ssl-mode=DISABLED dzvote_v2 < generate_service_id.sql
if %errorlevel% equ 0 (
    echo [OK] Service IDs generated successfully
) else (
    echo [ERROR] Failed to generate service IDs
)
echo.

echo ===================================
echo All scripts completed!
echo Please restart the backend service.
echo ===================================
pause


