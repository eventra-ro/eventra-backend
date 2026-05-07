# ═══════════════════════════════════════════════════════════════
# Eventra Backend — Dockerfile
# Multi-stage build: build cu Maven, rulare cu JRE minimal
# ═══════════════════════════════════════════════════════════════

# ── Stage 1: BUILD ──────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiază pom.xml primul — Docker cache pentru dependencies
# Dacă pom.xml nu s-a schimbat, re-folosește layer-ul cu dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copiază sursa și build-ează
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ── Stage 2: RUNTIME ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utilizator non-root pentru securitate
RUN addgroup -S eventra && adduser -S eventra -G eventra

# Copiază doar JAR-ul din stage-ul de build
COPY --from=build /app/target/*.jar app.jar

# Ownership corect
RUN chown eventra:eventra app.jar

USER eventra

# Port expus
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Pornire aplicație
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
