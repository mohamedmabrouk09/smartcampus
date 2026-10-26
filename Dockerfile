# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

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