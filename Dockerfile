# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy source code
COPY pom.xml .
COPY src ./src

# Build without tests (tests will run at container startup)
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install wait-for-it and curl
RUN apt-get update && \
    apt-get install -y wait-for-it curl && \
    rm -rf /var/lib/apt/lists/*

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Wait for database before starting the application
CMD ["sh", "-c", "wait-for-it db:5432 -- java -jar app.jar"]
