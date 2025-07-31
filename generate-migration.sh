#!/bin/bash

# Script pour générer automatiquement les migrations Flyway depuis les entités JPA
# Usage: ./generate-migration.sh

set -e

echo "🚀 Démarrage de la génération des migrations Flyway..."

# 1. Nettoyer les fichiers temporaires
echo "🧹 Nettoyage des fichiers temporaires..."
rm -f target/generated-schema.sql

# 2. Compiler le projet pour s'assurer que toutes les classes sont disponibles
echo "🔨 Compilation du projet..."
./mvnw clean compile -q

# 3. Générer le script DDL depuis les entités avec Spring Boot
echo "⚙️ Génération du script DDL..."
./mvnw spring-boot:run -Dspring.profiles.active=ddl-export -Dspring-boot.run.main-class=com.tempo.application.utils.DDLGenerator -q

# 4. Afficher le résultat
if [ -f "target/generated-schema.sql" ]; then
    echo "✅ Script DDL généré avec succès!"
    echo "📄 Contenu du script généré:"
    echo "$(head -n 10 target/generated-schema.sql)..."
    echo ""
    echo "📁 Fichiers créés :"
    ls -la target/generated-schema.sql 2>/dev/null || echo "Fichier DDL non trouvé"
    echo ""
    echo "📂 Migrations Flyway :"
    ls -la src/main/resources/db/migration/ 2>/dev/null || echo "Aucune migration trouvée"
else
    echo "❌ Échec de la génération du script DDL"
    exit 1
fi

echo ""
echo "🎉 Migration générée avec succès!"
echo "💡 Prochaines étapes :"
echo "   1. Vérifiez le contenu de la migration dans src/main/resources/db/migration/"
echo "   2. Démarrez PostgreSQL avec: docker-compose -f compose.dev.postgresql.yml up -d"
echo "   3. Appliquez la migration avec: ./db-dev-tools.sh migrate"
echo "   4. Ou démarrez l'application pour l'appliquer automatiquement" 