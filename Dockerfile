# syntax=docker/dockerfile:1
# Build : docker build --build-arg VERSION=1.0 -t smartcampus:1.0 .
ARG VERSION=1.0
FROM eclipse-temurin:17-jre-alpine AS base

WORKDIR /app

# Métadonnées OCI
LABEL org.opencontainers.image.title="SmartCampus API"
LABEL org.opencontainers.image.description="API HTTP Java 17 - authentification + PostgreSQL"
LABEL org.opencontainers.image.version="${VERSION}"
LABEL org.opencontainers.image.source="https://github.com/SRMOHAMED01/smartcampus"
LABEL org.opencontainers.image.created="1970-01-01T00:00:00Z"

# Création d'un utilisateur non root pour la sécurité
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# JAR produit par Jenkins (mvn clean package)
COPY target/smartcampus.jar /app/smartcampus.jar

# Données nécessaires à l'application
VOLUME ["/app/data"]
RUN mkdir -p /app/data && chown appuser:appgroup /app/data

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD ["sh", "-c", "wget -qO- http://localhost:${PORT}/health || exit 1"]

USER appuser:appgroup

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/smartcampus.jar ${PORT}"]