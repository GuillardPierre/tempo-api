# 🚀 Guide de Déploiement Heroku - Tempo API

## 📋 Prérequis

- [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli) installé
- Compte Heroku actif
- Git configuré avec votre projet

## 🔧 Configuration Initiale

### 1. Connexion à Heroku

```bash
heroku login
```

### 2. Création de l'application (si première fois)

```bash
./scripts/deploy-heroku.sh [nom-app]
```

## 🚀 Déploiement Automatique

### Script automatisé (Recommandé)

```bash
# Déploiement complet avec migrations
./scripts/deploy-heroku.sh tempo-api
```

## 🗄️ Gestion de la Base de Données

### Variables d'environnement automatiques

Heroku fournit automatiquement :

- `JDBC_DATABASE_URL` : URL de connexion PostgreSQL
- `JDBC_DATABASE_USERNAME` : Nom d'utilisateur
- `JDBC_DATABASE_PASSWORD` : Mot de passe
- `PORT` : Port d'écoute

### Exécution des migrations

```bash
# Via le script automatisé
heroku run --app tempo-api ./scripts/run-migrations-prod.sh

# Ou manuellement
heroku run --app tempo-api java -Dspring.profiles.active=prod -jar target/application-0.0.1-SNAPSHOT.jar
```

## 📊 Monitoring et Logs

### Voir les logs en temps réel

```bash
heroku logs --tail --app tempo-api
```

### Accéder à la console Heroku

```bash
heroku run bash --app tempo-api
```

### Vérifier le statut de l'application

```bash
heroku ps --app tempo-api
```

## 🔒 Sécurité

### Variables d'environnement sensibles

```bash
# Définir une clé secrète personnalisée (optionnel)
heroku config:set APP_SECRET_KEY=votre-cle-secrete --app tempo-api

# Voir toutes les variables
heroku config --app tempo-api
```

## 🧪 Tests de Production

### Vérifier la santé de l'application

```bash
# L'endpoint de santé est automatiquement configuré
curl https://tempo-api.herokuapp.com/actuator/health
```

### Tester la base de données

```bash
# Accéder à PostgreSQL
heroku pg:psql --app tempo-api

# Voir les informations de la base
heroku pg:info --app tempo-api
```

### Support

- [Documentation Heroku](https://devcenter.heroku.com/)
- [Logs de l'application](https://tempo-api.herokuapp.com/actuator/health)
