# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# JAR produit par Jenkins (mvn clean package)
COPY target/smartcampus.jar /app/smartcampus.jar

# Données nécessaires à l'application
VOLUME ["/app/data"]

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD ["sh", "-c", "wget -qO- http://localhost:${PORT}/health || exit 1"]

ENTRYPOINT ["sh", "-c", "exec java -jar /app/smartcampus.jar ${PORT}"]