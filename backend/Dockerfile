# Utiliser une image Maven avec Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Définir le répertoire de travail
WORKDIR /app

# Faire confiance au certificat racine d'Avast (inspection SSL/TLS sur le reseau de dev).
# Sur un reseau sans interception SSL, cet import est simplement ignore/inoffensif.
COPY avast-root.crt /tmp/avast-root.crt
RUN keytool -importcert -noprompt -trustcacerts -alias avast-ssl-scan \
    -file /tmp/avast-root.crt -cacerts -storepass changeit || true

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Télécharger les dépendances (mise en cache).
# Cache mount BuildKit : les artefacts deja telecharges survivent aux echecs.
# Retries wagon : retente les telechargements ratés au lieu d'abandonner.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# Copier le code source
COPY src ./src

# Compiler l'application
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# Image finale pour l'exécution
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl \
 && addgroup -S app && adduser -S app -G app

# Même confiance Avast au runtime (SMTP Gmail STARTTLS / HTTPS).
COPY avast-root.crt /tmp/avast-root.crt
RUN keytool -importcert -noprompt -trustcacerts -alias avast-ssl-scan \
    -file /tmp/avast-root.crt -cacerts -storepass changeit || true \
 && rm -f /tmp/avast-root.crt

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar
RUN chown -R app:app /app

USER app

# Render injecte PORT ; local / compose = 8080 (voir server.port=${PORT:8080})
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD curl -fsS "http://127.0.0.1:${PORT}/actuator/health" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
