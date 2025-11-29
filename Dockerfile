# Stage 1: Build the application
# We use a JDK image to have the 'javac' compiler available
FROM openjdk:21-jdk-slim AS build

WORKDIR /app

# Copy the source file
COPY CatalogApi.java .

# Compile the source. 
# This will generate CatalogApi.class and inner classes
RUN javac CatalogApi.java

# Stage 2: Run the application
# We start fresh to keep the final image clean and small
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy all compiled class files from the build stage
COPY --from=build /app/CatalogApi*.class ./

# Expose the port defined in the application
EXPOSE 8000

# Run the application
CMD ["java", "CatalogApi"]