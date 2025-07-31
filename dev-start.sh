#!/bin/bash

echo "🚀 Démarrage de l'environnement de développement PostgreSQL avec hot reload..."

# Arrêter les conteneurs existants
docker-compose -f compose.dev.postgresql.yml down

# Construire et démarrer les services
docker-compose -f compose.dev.postgresql.yml up --build

echo "✅ Environnement de développement PostgreSQL démarré!"
echo "📱 Application disponible sur : http://localhost:8080"
echo "🗄️ Base de données PostgreSQL sur : localhost:5432"
echo "🔄 Hot reload activé - les changements de code seront automatiquement détectés"
echo "🔥 LiveReload disponible sur le port 35729" 