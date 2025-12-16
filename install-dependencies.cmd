@echo off
echo Installing local dependencies to Maven repository...
echo.

REM Install events-contract
echo Installing events-contract.jar...
call demo\mvnw.cmd install:install-file -Dfile=demo\lib\events-contract.jar -DgroupId=org.example.restaurant -DartifactId=events-contract -Dversion=1.0-SNAPSHOT -Dpackaging=jar

REM Install restaurant-api-contracts
echo Installing restaurant_api_contracts.jar...
call demo\mvnw.cmd install:install-file -Dfile=demo\lib\restaurant_api_contracts.jar -DgroupId=com.example -DartifactId=Restaurant -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar

echo.
echo Dependencies installed successfully!
echo You can now build the project with: docker compose up -d --build
pause
