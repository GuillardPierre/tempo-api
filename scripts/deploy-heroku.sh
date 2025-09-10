#!/bin/bash

# Script de déploiement automatisé pour Heroku
# Usage: ./scripts/deploy-heroku.sh [app-name]

set -e

# Nom de l'application Heroku (peut être passé en paramètre)
APP_NAME=${1:-"tempo-api"}

echo "🚀 Déploiement de Tempo API sur Heroku..."
echo "📱 Application: $APP_NAME"

# Vérifier que Heroku CLI est installé
if ! command -v heroku &> /dev/null; then
    echo "❌ Erreur: Heroku CLI n'est pas installé"
    echo "Installez-le depuis: https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Vérifier la connexion à Heroku
echo "🔐 Vérification de la connexion Heroku..."
if ! heroku auth:whoami &> /dev/null; then
    echo "❌ Erreur: Non connecté à Heroku"
    echo "Exécutez: heroku login"
    exit 1
fi

# Vérifier si l'application existe
if ! heroku apps:info --app "$APP_NAME" &> /dev/null; then
    echo "📱 Création de l'application Heroku: $APP_NAME"
    heroku create "$APP_NAME" --region eu
fi


# # Ajouter l'add-on PostgreSQL si pas déjà présent
# echo "🗄️  Vérification de l'add-on PostgreSQL..."
# if ! heroku addons:info postgresql --app "$APP_NAME" &> /dev/null; then
#     echo "➕ Ajout de l'add-on PostgreSQL..."
#     heroku addons:create heroku-postgresql:essential-0 --app "$APP_NAME"
# fi

# Compiler l'application
echo "🔨 Compilation de l'application..."
./mvnw clean package -DskipTests

# Déployer sur Heroku
echo "📤 Déploiement sur Heroku..."
git add .
git commit -m "Deploy to Heroku - $(date)" || true
git push heroku main

# Exécuter les migrations
echo "🔄 Exécution des migrations..."
heroku run --app "$APP_NAME" ./scripts/run-migrations-prod.sh

# Ouvrir l'application
echo "🌐 Ouverture de l'application..."
heroku open --app "$APP_NAME"

echo "✅ Déploiement terminé avec succès!"
echo "📊 Logs: heroku logs --tail --app $APP_NAME"
echo "🔧 Console: heroku run bash --app $APP_NAME"
