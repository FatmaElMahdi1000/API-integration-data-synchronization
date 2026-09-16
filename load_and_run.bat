@echo off
setlocal

echo ============================================================
echo  Varthak Assessment API - Offline Package Launcher
echo ============================================================
echo.

if not exist "oracle-db.tar" (
    echo [ERROR] oracle-db.tar not found in current directory.
    exit /b 1
)
if not exist "varthak-app.tar" (
    echo [ERROR] varthak-app.tar not found in current directory.
    exit /b 1
)

echo [1/3] Loading Oracle XE image (this may take a while)...
docker load -i oracle-db.tar
if errorlevel 1 goto :error

echo [2/3] Loading application image...
docker load -i varthak-app.tar
if errorlevel 1 goto :error

echo [3/3] Starting containers...
docker compose up -d --no-build
if errorlevel 1 goto :error

echo.
echo ============================================================
echo  Deployment complete!
echo  API:            http://localhost:3030
echo  Swagger UI:     http://localhost:3030/swagger-ui.html
echo  OpenAPI Spec:   http://localhost:3030/v3/api-docs
echo ============================================================
endlocal
exit /b 0

:error
echo.
echo [ERROR] Deployment failed. Please verify Docker Desktop is running.
endlocal
exit /b 1