# 1. Build Spring Boot JAR
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew \
    && ./gradlew clean bootJar --no-daemon -x test \
    && JAR_FILE="$(find build/libs \
            -maxdepth 1 \
            -type f \
            -name '*.jar' \
            ! -name '*-plain.jar' \
            | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /app/app.jar

# 1. Run Spring Boot JAR
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/kho903/JihunPage" \
      org.opencontainers.image.description="JihunPage Spring Boot backend"

RUN addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder \
    --chown=spring:spring \
    /app/app.jar \
    /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
