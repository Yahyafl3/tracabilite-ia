@echo off
echo ========================================
echo   REBUILD ET REDEMARRAGE DOCKER
echo ========================================
echo.

cd /d "%~dp0"

echo Arret des conteneurs...
docker-compose down

echo.
echo Reconstruction et demarrage...
docker-compose up -d --build

echo.
echo ========================================
echo   TERMINE !
echo ========================================
echo.
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080
echo.
pause
