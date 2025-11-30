# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy configuration and source
COPY pom.xml .
COPY src ./src

# Build the JAR (skipping tests for speed in build stage, usually you'd run them)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR from the maven stage
# The name includes version from pom.xml
COPY --from=build /app/target/catalog-api-1.0.0-SNAPSHOT.jar app.jar

# Expose the port defined in the application
EXPOSE 8000

# Run the application
CMD ["java", "-jar", "app.jar"]