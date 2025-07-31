#!/bin/bash

# Outils de développement pour la gestion de la base de données
# Usage: ./db-dev-tools.sh [commande]

set -e

function show_help() {
    echo "🛠️ Outils de développement Base de Données - Tempo API"
    echo ""
    echo "Usage: ./db-dev-tools.sh [commande]"
    echo ""
    echo "Commandes disponibles:"
    echo "  generate     - Générer une nouvelle migration depuis les entités"
    echo "  migrate      - Appliquer toutes les migrations en attente"
    echo "  clean        - Nettoyer complètement la base de données"
    echo "  reset        - Nettoyer et re-créer la base de données"
    echo "  info         - Afficher l'état des migrations"
    echo "  validate     - Valider les migrations"
    echo "  repair       - Réparer le schema de version Flyway"
    echo "  baseline     - Initialiser Flyway sur une base existante"
    echo "  test-config  - Tester différentes configurations de BDD"
    echo "  help         - Afficher cette aide"
    echo ""
    echo "Variables d'environnement:"
    echo "  DATABASE_URL - URL de la base de données (défaut: jdbc:postgresql://localhost:5432/tempo_db)"
    echo "  DB_USERNAME  - Nom d'utilisateur (défaut: tempo_user)"
    echo "  DB_PASSWORD  - Mot de passe (défaut: tempo_pass)"
    echo ""
    echo "Configurations de test:"
    echo "  ./db-dev-tools.sh test-config docker    # Test avec PostgreSQL Docker"
    echo "  ./db-dev-tools.sh test-config external  # Test avec PostgreSQL externe"
}

function generate_migration() {
    echo "🔄 Génération d'une nouvelle migration..."
    ./generate-migration.sh
}

function migrate() {
    echo "⬆️ Application des migrations..."
    ./mvnw flyway:migrate
}

function clean_db() {
    echo "⚠️ ATTENTION: Cette opération va supprimer TOUTES les données!"
    read -p "Êtes-vous sûr de vouloir continuer? (yes/no): " confirm
    if [ "$confirm" == "yes" ]; then
        echo "🧹 Nettoyage de la base de données..."
        ./mvnw flyway:clean
        echo "✅ Base de données nettoyée"
    else
        echo "❌ Opération annulée"
    fi
}

function reset_db() {
    echo "🔄 Remise à zéro complète de la base de données..."
    clean_db
    if [ $? -eq 0 ]; then
        echo "📦 Régénération du schéma..."
        generate_migration
        migrate
        echo "✅ Base de données réinitialisée"
    fi
}

function info() {
    echo "📊 État des migrations:"
    ./mvnw flyway:info
}

function validate() {
    echo "✅ Validation des migrations:"
    ./mvnw flyway:validate
}

function repair() {
    echo "🔧 Réparation du schéma Flyway:"
    ./mvnw flyway:repair
}

function baseline() {
    echo "🎯 Initialisation Flyway:"
    ./mvnw flyway:baseline
}

function test_config() {
    local config_type=${1:-"help"}
    
    case "$config_type" in
        "docker")
            echo "🐳 Test de la configuration Docker..."
            echo "1. Vérification de PostgreSQL Docker:"
            if docker-compose -f compose.dev.postgresql.yml ps | grep -q postgres; then
                echo "✅ PostgreSQL Docker est actif"
            else
                echo "❌ PostgreSQL Docker n'est pas actif"
                echo "💡 Démarrez-le avec: docker-compose -f compose.dev.postgresql.yml up -d"
                return 1
            fi
            
            echo "2. Test de connexion:"
            DATABASE_URL="jdbc:postgresql://localhost:5432/tempo_db" \
            DB_USERNAME="tempo_user" \
            DB_PASSWORD="tempo_pass" \
            ./mvnw flyway:info -q
            ;;
            
        "external")
            echo "🌐 Test de la configuration base externe..."
            echo "⚠️ Assurez-vous qu'une instance PostgreSQL tourne en local sur le port 5433"
            echo ""
            
            # Test de connectivité basique
            echo "1. Test de connectivité PostgreSQL:"
            if pg_isready -h localhost -p 5433 -U tempo_user 2>/dev/null; then
                echo "✅ PostgreSQL externe accessible"
            else
                echo "❌ PostgreSQL externe non accessible"
                echo "💡 Conseils:"
                echo "   - Installez PostgreSQL localement"
                echo "   - Configurez-le sur le port 5433"
                echo "   - Créez la base tempo_db et l'utilisateur tempo_user"
                return 1
            fi
            
            echo "2. Test de connexion Flyway:"
            DATABASE_URL="jdbc:postgresql://localhost:5433/tempo_db" \
            DB_USERNAME="tempo_user" \
            DB_PASSWORD="tempo_pass" \
            ./mvnw flyway:info -q
            ;;
            
        "help"|*)
            echo "Usage: ./db-dev-tools.sh test-config [docker|external]"
            echo ""
            echo "Configurations disponibles:"
            echo "  docker   - Test avec PostgreSQL en Docker (port 5432)"
            echo "  external - Test avec PostgreSQL externe (port 5433)"
            echo ""
            echo "Exemples:"
            echo "  ./db-dev-tools.sh test-config docker"
            echo "  ./db-dev-tools.sh test-config external"
            ;;
    esac
}

# Vérifier si Maven wrapper existe
if [ ! -f "./mvnw" ]; then
    echo "❌ Erreur: Maven wrapper (mvnw) non trouvé"
    echo "Assurez-vous d'être dans le répertoire racine du projet"
    exit 1
fi

# Parser les arguments
case "${1:-help}" in
    "generate")
        generate_migration
        ;;
    "migrate")
        migrate
        ;;
    "clean")
        clean_db
        ;;
    "reset")
        reset_db
        ;;
    "info")
        info
        ;;
    "validate")
        validate
        ;;
    "repair")
        repair
        ;;
    "baseline")
        baseline
        ;;
    "test-config")
        test_config "${2:-help}"
        ;;
    "help"|*)
        show_help
        ;;
esac 