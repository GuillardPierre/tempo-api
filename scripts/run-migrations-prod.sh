#!/bin/bash

# Script pour exécuter les migrations Flyway en production sur Heroku
# Usage: ./scripts/run-migrations-prod.sh

set -e

echo "🚀 Démarrage des migrations de production..."

# Vérifier que nous sommes sur Heroku
if [ -z "$JDBC_DATABASE_URL" ]; then
    echo "❌ Erreur: Variables d'environnement Heroku non trouvées"
    echo "Ce script doit être exécuté sur Heroku ou avec les variables d'environnement appropriées"
    exit 1
fi

echo "✅ Variables d'environnement Heroku détectées"
echo "📊 URL de base de données: ${JDBC_DATABASE_URL:0:50}..."

# Compiler l'application si nécessaire
if [ ! -f "target/application-0.0.1-SNAPSHOT.jar" ]; then
    echo "🔨 Compilation de l'application..."
    ./mvnw clean package -DskipTests
fi

# Exécuter les migrations via Spring Boot
echo "🔄 Exécution des migrations Flyway..."
java -Dspring.profiles.active=prod \
     -Dspring.flyway.enabled=true \
     -Dspring.jpa.hibernate.ddl-auto=none \
     -jar target/application-0.0.1-SNAPSHOT.jar \
     --spring.flyway.migrate-on-startup=true

echo "✅ Migrations terminées avec succès!"
echo "🌐 L'application est prête à recevoir du trafic"
