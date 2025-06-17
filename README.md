# 🕐 Tempo API

API REST pour la gestion du temps de travail développée avec **Spring Boot 3.4** et **Java 21**.

## 📋 Description

Tempo est une application de gestion des temps de travail qui permet de :

- ⏱️ Enregistrer des temps de travail ponctuels
- 🔄 Créer des séries récurrentes de temps de travail
- 📊 Générer des statistiques par catégories
- 📈 Visualiser les données par jour/semaine/mois
- 🔐 Authentification JWT

## 🛠️ Technologies

- **Backend** : Spring Boot 3.4, Java 21
- **Base de données** : PostgreSQL 16 (+ support MySQL 8.0)
- **Migration** : Flyway
- **Sécurité** : Spring Security + JWT
- **Build** : Maven
- **Containerisation** : Docker + Docker Compose

## 🚀 Démarrage

### 📋 Prérequis

- Docker et Docker Compose

### 🔧 Mode Développement (avec Hot Reload)

Pour développer avec rechargement automatique :

**MySQL (version classique) :**

```bash
# Avec le script (recommandé)
bash dev-start.sh

# Ou directement
docker-compose -f compose.dev.yml up --build
```

**PostgreSQL (version moderne) :**

```bash
# Avec le script PostgreSQL
bash dev-start-postgresql.sh

# Ou directement
docker-compose -f compose.dev.postgresql.yml up --build
```

**Fonctionnalités dev :**

- 🔄 **Hot reload** automatique des changements de code
- 🔥 **LiveReload** sur port 35729
- 📦 Cache Maven pour builds rapides
- 🐛 DevTools Spring Boot activé

### 🏭 Mode Production

Pour lancer en mode production :

```bash
# Avec le script (recommandé)
bash start.sh

# Ou directement
docker-compose up --build
```

**Optimisations prod :**

- 📦 Image Docker multi-stage optimisée
- ⚡ JAR pré-compilé pour démarrage rapide
- 🎯 Image JRE légère (sans Maven/JDK)

## 📱 URLs

| Service        | URL                   | Description                                             |
| -------------- | --------------------- | ------------------------------------------------------- |
| **API**        | http://localhost:8080 | Application principale                                  |
| **MySQL**      | localhost:3306        | MySQL (user: `myuser`, password: `secret`)              |
| **PostgreSQL** | localhost:5432        | PostgreSQL (user: `tempo_user`, password: `tempo_pass`) |
| **LiveReload** | localhost:35729       | Rechargement auto (dev seulement)                       |

## 🗄️ Base de données

- **Base** : `mydatabase`
- **Utilisateur** : `myuser` / `secret`
- **Root** : `root` / `verysecret`
- **Migrations** : Gérées automatiquement par Flyway

## 📊 Endpoints principaux

```
GET  /api/stats/categories?userId={id}&from={date}&to={date}  # Statistiques par catégorie
GET  /api/stats/total?userId={id}&from={date}&to={date}&type={week|month|year}  # Temps total
POST /api/auth/login                                         # Connexion
POST /api/worktime                                          # Créer temps de travail
GET  /api/worktime                                          # Lister temps de travail
```

## 🏗️ Architecture

```
src/
├── main/java/com/tempo/application/
│   ├── controller/          # Contrôleurs REST
│   ├── service/             # Logique métier
│   ├── repository/          # Accès données
│   ├── model/               # Entités et DTOs
│   ├── security/            # Configuration sécurité
│   └── utils/               # Utilitaires
└── main/resources/
    ├── db/migration/        # Scripts Flyway
    └── application*.properties  # Configuration
```

## 🔄 Scripts disponibles

| Script                    | Usage          | Description                        |
| ------------------------- | -------------- | ---------------------------------- |
| `dev-start.sh`            | Développement  | Lance avec hot reload              |
| `dev-start-postgresql.sh` | Développement  | Lance avec PostgreSQL + hot reload |
| `start.sh`                | Production     | Lance en mode optimisé             |
| `generate-migration.sh`   | Génération DDL | Crée migrations depuis entités     |

## 🗂️ Génération DDL

Générez automatiquement les migrations de base de données depuis vos entités JPA :

### 🚀 Génération rapide

```bash
# Générer une migration depuis les entités
./generate-migration.sh
```

### 🛠️ Génération manuelle

```bash
# Avec Maven
./mvnw spring-boot:run -Dspring.profiles.active=ddl-export

# Résultat dans :
# - target/generated-schema.sql (script SQL)
# - src/main/resources/db/migration/ (migration Flyway)
```

### 📋 Quand utiliser

- ✅ Après modification d'entités JPA (`@Entity`)
- ✅ Ajout de nouvelles tables
- ✅ Changement de structure de données
- ❌ **Pas en développement normal** (déjà désactivé)

## 🛑 Arrêt des services

```bash
# Mode développement
docker-compose -f compose.dev.yml down

# Mode production
docker-compose down
```

## 🔍 Debug et Logs

```bash
# Voir les logs en temps réel
docker-compose logs -f api

# Accéder au conteneur
docker-compose exec api bash
```

## 🚧 Développement

### Hot Reload

1. Modifiez un fichier `.java` dans `src/`
2. Sauvegardez → L'application redémarre automatiquement
3. Spring Boot DevTools détecte les changements

### Base de données

- Les migrations Flyway s'exécutent automatiquement
- Fichiers dans `src/main/resources/db/migration/`

## 📝 Variables d'environnement

| Variable         | Défaut                               | Description         |
| ---------------- | ------------------------------------ | ------------------- |
| `DB_URL`         | `jdbc:mysql://mysql:3306/mydatabase` | URL base de données |
| `DB_USERNAME`    | `myuser`                             | Utilisateur DB      |
| `DB_PASSWORD`    | `secret`                             | Mot de passe DB     |
| `APP_SECRET_KEY` | `secret...`                          | Clé JWT             |
| `JAVA_OPTS`      | `-Xmx512m -Xms256m`                  | Options JVM         |

---

## 🏃‍♂️ Démarrage rapide

1. **Clone du projet**

   ```bash
   git clone <repo-url>
   cd tempo-api-re
   ```

2. **Lancement développement**

   ```bash
   bash dev-start.sh
   ```

3. **Test de l'API**
   ```bash
   curl http://localhost:8080/api/health
   ```

Votre environnement Tempo est prêt ! 🎉
