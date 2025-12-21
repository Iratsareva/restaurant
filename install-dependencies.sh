#!/bin/bash
echo "Installing local dependencies to Maven repository..."
echo "This script installs JAR dependencies that are used by multiple services."
echo

# Install events-contract (used by demo, audit-service, notification-service)
echo "Installing events-contract.jar..."
./demo/mvnw install:install-file \
  -Dfile=demo/lib/events-contract.jar \
  -DgroupId=org.example.restaurant \
  -DartifactId=events-contract \
  -Dversion=1.0-SNAPSHOT \
  -Dpackaging=jar

# Install restaurant-api-contracts (used by demo)
echo "Installing restaurant_api_contracts.jar..."
./demo/mvnw install:install-file \
  -Dfile=demo/lib/restaurant_api_contracts.jar \
  -DgroupId=com.example \
  -DartifactId=Restaurant \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar

# Copy events-contract.jar to other services that need it
echo "Copying events-contract.jar to audit-service..."
mkdir -p audit-service/audit-service/lib
cp demo/lib/events-contract.jar audit-service/audit-service/lib/events-contract.jar

echo "Copying events-contract.jar to notification-service..."
mkdir -p notification-service/notification-service/lib
cp demo/lib/events-contract.jar notification-service/notification-service/lib/events-contract.jar

echo
echo "Dependencies installed and JAR files copied successfully!"
echo "You can now build the project with: docker compose up -d --build"
echo
echo "Note: This script must be run before building the project on a new device."
