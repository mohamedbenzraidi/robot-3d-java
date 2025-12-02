@echo off
REM ========================================
REM Windows Batch Script to Setup Database
REM ========================================

echo.
echo ==========================================
echo   3D Museum Tour - Database Setup
echo ==========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed or not in PATH
    echo Please install Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo [1/5] Checking Docker...
echo ✓ Docker is installed

REM Stop and remove existing container if it exists
echo.
echo [2/5] Removing old container (if exists)...
docker rm -f my-postgres-container >nul 2>&1
echo ✓ Old container removed

REM Create and start new PostgreSQL container
echo.
echo [3/5] Creating new PostgreSQL container...
docker run --name my-postgres-container ^
  -e POSTGRES_USER=user ^
  -e POSTGRES_PASSWORD=password ^
  -e POSTGRES_DB=tour_3d_db ^
  -p 5432:5432 ^
  -d postgres

if %errorlevel% neq 0 (
    echo [ERROR] Failed to create container
    pause
    exit /b 1
)

echo ✓ Container created successfully

REM Wait for PostgreSQL to be ready
echo.
echo [4/5] Waiting for PostgreSQL to start...
timeout /t 5 /nobreak >nul
echo ✓ PostgreSQL is ready

REM Run SQL setup script
echo.
echo [5/5] Creating database schema and inserting data...
docker exec -i my-postgres-container psql -U user -d tour_3d_db < setup_database.sql

if %errorlevel% neq 0 (
    echo [ERROR] Failed to setup database
    echo Checking container logs:
    docker logs my-postgres-container
    pause
    exit /b 1
)

echo ✓ Database setup complete

REM Verify installation
echo.
echo ==========================================
echo   Verification
echo ==========================================
docker exec my-postgres-container psql -U user -d tour_3d_db -c "SELECT COUNT(*) as total_paintings FROM Paintings;"

echo.
echo ==========================================
echo   SUCCESS! Database is ready to use
echo ==========================================
echo.
echo Container name: my-postgres-container
echo Database: tour_3d_db
echo Username: user
echo Password: password
echo Port: 5432
echo.
echo To stop the container: docker stop my-postgres-container
echo To start the container: docker start my-postgres-container
echo To view logs: docker logs my-postgres-container
echo.
pause

