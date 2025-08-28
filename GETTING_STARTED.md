# 🚀 Guide de Démarrage - Configuration Migrations

Ce guide vous explique comment démarrer et tester votre système de migrations Hibernate DDL + Flyway.

## 📋 Prérequis

- Java 21+
- Maven
- Docker et Docker Compose
- Git Bash (Windows)

## 🐘 1. Démarrer PostgreSQL

### Option A : Avec Docker Compose (Recommandé)

```bash
# Démarrer PostgreSQL en arrière-plan
docker-compose -f compose.dev.postgresql.yml up -d

# Vérifier que PostgreSQL est démarré
docker-compose -f compose.dev.postgresql.yml logs
```

### Option B : Avec le script fourni

```bash
# Utiliser le script de développement
./dev-start-postgresql.sh
```

### Option C : PostgreSQL Local

Si vous avez PostgreSQL installé localement :

```bash
# Variables d'environnement
export DATABASE_URL="jdbc:postgresql://localhost:5432/tempo_db"
export DB_USERNAME="tempo_user"
export DB_PASSWORD="tempo_pass"

# Créer la base de données si nécessaire
psql -U postgres -c "CREATE DATABASE tempo_db;"
psql -U postgres -c "CREATE USER tempo_user WITH PASSWORD 'tempo_pass';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE tempo_db TO tempo_user;"
```

## ✅ 2. Vérifier la Connexion Base de Données

```bash
# Tester la connexion avec Flyway
./db-dev-tools.sh info
```

## 🗄️ 3. Appliquer la Migration Initiale

```bash
# Appliquer toutes les migrations en attente
./db-dev-tools.sh migrate
```

**Résultat attendu :**

```
Successfully applied 1 migration to schema "public", now at version v001.
```

## 🔍 4. Vérifier l'État des Migrations

```bash
# Voir l'état actuel
./db-dev-tools.sh info
```

**Résultat attendu :**

```
Schema version: 001
+----------+---------+---------------------+------+--------------+---------+
| Category | Version | Description         | Type | Installed On | State   |
+----------+---------+---------------------+------+--------------+---------+
| Success  | 001     | Initial schema      | SQL  | ...          | Success |
+----------+---------+---------------------+------+--------------+---------+
```

## 🔧 5. Tester les Outils de Développement

### Valider les Migrations

```bash
./db-dev-tools.sh validate
```

### Voir l'État Détaillé

```bash
./db-dev-tools.sh info
```

### Tester une Réinitialisation (DÉVELOPPEMENT SEULEMENT)

```bash
# ATTENTION: Supprime toutes les données!
./db-dev-tools.sh reset
```

## ⚠️ 6. Résolution des Problèmes Courants

### Problème : "Unable to connect to the database"

**Solutions :**

1. **Vérifier PostgreSQL :**

   ```bash
   docker-compose -f compose.dev.postgresql.yml ps
   ```

2. **Redémarrer PostgreSQL :**

   ```bash
   docker-compose -f compose.dev.postgresql.yml down
   docker-compose -f compose.dev.postgresql.yml up -d
   ```

3. **Vérifier les variables d'environnement :**
   ```bash
   echo $DATABASE_URL
   echo $DB_USERNAME
   echo $DB_PASSWORD
   ```

### Problème : "Schema-history table not found"

**Solution :**

```bash
# Initialiser Flyway sur une base existante
./db-dev-tools.sh baseline
```

### Problème : "Migration checksum mismatch"

**Solution :**

```bash
# Réparer le schéma Flyway
./db-dev-tools.sh repair
```

## 🏁 7. Tester l'Application

Une fois les migrations appliquées :

```bash
# Démarrer l'application
./mvnw spring-boot:run

# Ou avec le profil dev
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

**Vérifications :**

- L'application démarre sans erreur
- Les logs Flyway montrent "Successfully applied X migrations"
- L'API est accessible sur http://localhost:8080

## 📊 8. Vérifier les Tables Créées

Connectez-vous à PostgreSQL pour vérifier :

```bash
# Avec Docker
docker exec -it $(docker-compose -f compose.dev.postgresql.yml ps -q postgres) psql -U tempo_user -d tempo_db

# Localement
psql -U tempo_user -d tempo_db
```

```sql
-- Lister toutes les tables
\dt

-- Vérifier le schéma d'une table
\d "user"
\d category
\d worktime

-- Voir l'historique Flyway
SELECT * FROM flyway_schema_history;
```
