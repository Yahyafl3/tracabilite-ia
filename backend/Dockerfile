# Utiliser une image Maven avec Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Télécharger les dépendances (mise en cache)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler l'application
RUN mvn clean package -DskipTests

# Image finale pour l'exécution
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
