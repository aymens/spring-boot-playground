# @claude can we update this whole dockerfile to intelligently multistage build this to be independent from existing /target prior build? with no mvn tests obviously? your thoughts?
# notice that this is a multi module project, and the parent, or maybe even the root parent that needs to be build inside the build stage

FROM eclipse-temurin:22-jre-alpine

RUN addgroup -S -g 1001 appuser && \
    adduser -S -u 1001 -G appuser appuser && \
    apk add --no-cache curl

WORKDIR /app

#COPY target/app-*.jar app.jar
COPY target/app*.jar app.jar

RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose SBA port
EXPOSE 8088

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8088/actuator/health || exit 1

# Run with JVM optimizations
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]