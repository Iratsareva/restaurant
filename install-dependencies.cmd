@echo off
echo Installing local dependencies to Maven repository...
echo This script installs JAR dependencies that are used by multiple services.
echo.

REM Install events-contract (used by demo, audit-service, notification-service)
echo Installing events-contract.jar...
call demo\mvnw.cmd install:install-file -Dfile=demo\lib\events-contract.jar -DgroupId=org.example.restaurant -DartifactId=events-contract -Dversion=1.0-SNAPSHOT -Dpackaging=jar

REM Install restaurant-api-contracts (used by demo)
echo Installing restaurant_api_contracts.jar...
call demo\mvnw.cmd install:install-file -Dfile=demo\lib\restaurant_api_contracts.jar -DgroupId=com.example -DartifactId=Restaurant -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar

REM Copy events-contract.jar to other services that need it
echo Copying events-contract.jar to audit-service...
if not exist "audit-service\audit-service\lib" mkdir "audit-service\audit-service\lib"
copy "demo\lib\events-contract.jar" "audit-service\audit-service\lib\events-contract.jar"

echo Copying events-contract.jar to notification-service...
if not exist "notification-service\notification-service\lib" mkdir "notification-service\notification-service\lib"
copy "demo\lib\events-contract.jar" "notification-service\notification-service\lib\events-contract.jar"

echo.
echo Dependencies installed and JAR files copied successfully!
echo You can now build the project with: docker compose up -d --build
echo.
echo Note: This script must be run before building the project on a new device.
pause
