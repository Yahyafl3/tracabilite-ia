# Build portable — truststore officiel de l'image JDK/Maven uniquement.
# Ne pas copier de certificat Avast local (absent en CI / Linux standard).
# Voir docs/MAVEN_PKIX_DIAGNOSTIC.md pour le chemin local Windows + Avast.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
RUN chmod +x mvnw

# Télécharger les dépendances (mise en cache).
# Cache mount BuildKit : les artefacts déjà téléchargés survivent aux échecs.
# Retries wagon : retente les téléchargements ratés au lieu d'abandonner.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl \
 && addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar
RUN chown -R app:app /app

USER app

# Render injecte PORT ; local / compose = 8080 (voir server.port=${PORT:8080})
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD curl -fsS "http://127.0.0.1:${PORT}/actuator/health" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
