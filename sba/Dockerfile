FROM eclipse-temurin:22-jre-alpine

RUN addgroup -S -g 1001 appuser && \
    adduser -S -u 1001 -G appuser appuser && \
    apk add --no-cache curl

WORKDIR /app

COPY target/sba-*.jar app.jar

RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose SBA port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run with JVM optimizations
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]