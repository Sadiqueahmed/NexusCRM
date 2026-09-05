# =============================================================================
# NexusCRM Backend — Multi-stage Dockerfile
# =============================================================================
# Stage 1: Maven build (compiles the Spring Boot application)
# Stage 2: Minimal JRE runtime (runs the fat JAR)
# =============================================================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn package -DskipTests -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r nexuscrm && useradd -r -g nexuscrm nexuscrm

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Install curl for Docker healthchecks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Switch to non-root user
USER nexuscrm

# Expose Spring Boot port
EXPOSE 8080

# JVM tuning for containerized environments
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
