# Multi-stage build for Spring Boot app
# Stage 1: Build the JAR
FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

# Copy Gradle build files
COPY build.gradle settings.gradle ./

# Copy source code
COPY src/ src/

# Build the JAR (skip tests for faster build in container)
RUN gradle bootJar --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Install curl for health check
RUN apk add --no-cache curl

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
