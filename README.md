# SmartCampus

TP2 — Versionnement et collaboration avec Git et GitHub
TP4 — Conteneurisation avec Docker et Docker Compose

## Objectif
Utiliser Git, GitHub, Jira, Jenkins, Docker et Docker Compose pour déployer
une API Java (HTTP) avec son service d'authentification et sa base PostgreSQL.

## Ticket Jira
- SC-11 : Implementer LoginService
- TP4 : Chaîne DevOps complète (Jenkins → Docker → Compose)

## Technologies
- Java 17 (Maven)
- JUnit 5
- PostgreSQL 15 (pilote JDBC)
- Docker / Docker Compose
- Jenkins
- Git / GitHub

## Structure
- `src/main/java/...` : code source (API HTTP + LoginService + UserService JDBC)
- `src/test/java/...` : tests unitaires
- `docs/` : documentation
- `docker/init.sql` : initialisation de la base de données
- `Dockerfile` : image conteneur
- `compose.yaml` : orchestration API + PostgreSQL
- `Jenkinsfile` : pipeline CI (build → test → docker build)

## Lancer l'application

```bash
# Build the image and launch the API + PostgreSQL.
# The Dockerfile runs the Maven build automatically.
docker compose up -d --build

# Alternatively, build and run the image directly:
# docker build -t smartcampus:1.0 .
```

Endpoints :
- `GET /health` → `{"status":"UP"}`
- `GET /login?email=...&password=...` → `{"login":true|false}`
- `GET /` → `{"service":"SmartCampus","version":"1.0"}`

## Chaîne DevOps (Jenkins)

1. `Checkout` — checkout SCM
2. `Build` — `mvn -B clean package -DskipTests`
3. `Test` — `mvn -B test`
4. `Docker Build` — `docker build -t smartcampus:1.0 .`
5. `archiveArtifacts` — sauvegarde `target/smartcampus.jar`

Diagnostiquer les erreurs : `docker compose logs -f`