# Étape 1 : build de l'application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : image d'exécution
FROM eclipse-temurin:21-jre
WORKDIR /app

# Installation de netcat
RUN apt-get update && apt-get install -y netcat-openbsd

COPY --from=build /app/target/application-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Variables d'environnement (optionnel)
ENV JAVA_OPTS=""

# Commande de lancement
ENTRYPOINT ["sh", "-c", "/wait-for-it.sh mysql:3306 -- java $JAVA_OPTS -jar app.jar"]
