# ==========================================
# Stage 1: Build the Spring Boot Application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the pom.xml and download dependencies
# This caches the maven dependencies so they aren't downloaded every time the code changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the Java Application
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the generated JAR file from Stage 1
COPY --from=build /app/target/scalelink-backend-1.0.0.jar app.jar

# Expose the port the app runs on
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
