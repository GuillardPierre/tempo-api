# 🗄️ Gestion des Migrations Base de Données - Tempo API

Ce projet utilise **Hibernate DDL Export** combiné avec **Flyway** pour une gestion automatisée et versionnée des migrations de base de données.

## 🏗️ Architecture

- **Hibernate DDL Export** : Génère automatiquement les scripts SQL depuis les entités JPA
- **Flyway** : Gère le versioning et l'application des migrations
- **PostgreSQL** : Base de données principale (compatible MySQL)

## 🚀 Démarrage Rapide

### 1. Première Migration

```bash
# Générer la migration initiale depuis vos entités
./generate-migration.sh

# Appliquer la migration
./db-dev-tools.sh migrate
```

### 2. Développement Quotidien

```bash
# Voir l'état des migrations
./db-dev-tools.sh info

# Appliquer les migrations en attente
./db-dev-tools.sh migrate

# Valider les migrations
./db-dev-tools.sh validate
```

### 3. Modifications du Modèle

Quand vous modifiez vos entités JPA :

```bash
# 1. Générer une nouvelle migration
./db-dev-tools.sh generate

# 2. Vérifier la migration générée
cat src/main/resources/db/migration/V*__*.sql

# 3. Appliquer la migration
./db-dev-tools.sh migrate
```

## 🛠️ Outils Disponibles

### Scripts Principaux

- `./generate-migration.sh` - Génère une migration depuis les entités
- `./db-dev-tools.sh [commande]` - Outils de gestion de la BDD

### Commandes db-dev-tools.sh

| Commande   | Description                     |
| ---------- | ------------------------------- |
| `generate` | Générer une nouvelle migration  |
| `migrate`  | Appliquer toutes les migrations |
| `clean`    | Nettoyer complètement la BDD    |
| `reset`    | Nettoyer et re-créer la BDD     |
| `info`     | Afficher l'état des migrations  |
| `validate` | Valider les migrations          |
| `repair`   | Réparer le schéma Flyway        |
| `baseline` | Initialiser Flyway              |

### Commandes Maven

```bash
# Appliquer les migrations
./mvnw flyway:migrate

# Voir l'état
./mvnw flyway:info

# Nettoyer (ATTENTION: supprime tout)
./mvnw flyway:clean

# Générer DDL uniquement
./mvnw exec:java -Dexec.mainClass="com.tempo.application.utils.DDLGenerator"
```

## ⚙️ Configuration

### Profils Spring

- **default** : Production avec Flyway activé
- **dev** : Développement avec logs DDL
- **postgresql** : Configuration PostgreSQL
- **ddl-export** : Export DDL uniquement (usage interne)

### Variables d'Environnement

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/tempo_db"
export DB_USERNAME="tempo_user"
export DB_PASSWORD="tempo_pass"
```

### Configuration application.properties

```properties
# Flyway activé
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Hibernate DDL désactivé (Flyway prend le relais)
spring.jpa.hibernate.ddl-auto=none
```

## 📁 Structure des Fichiers

```
proyecto/
├── src/main/resources/db/migration/     # Migrations Flyway
│   ├── V001__Initial_schema.sql
│   └── V002__Add_new_feature.sql
├── src/main/java/.../utils/
│   └── DDLGenerator.java                # Générateur DDL
├── generate-migration.sh               # Script génération
├── db-dev-tools.sh                    # Outils développement
└── target/
    └── generated-schema.sql            # DDL temporaire
```

## 🔄 Workflow de Développement

### 1. Modification d'Entité

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    // Nouvelle colonne ajoutée
    @Column(name = "full_name")
    private String fullName;
}
```

### 2. Génération de Migration

```bash
./db-dev-tools.sh generate
```

### 3. Vérification et Application

```bash
# Vérifier la migration générée
cat src/main/resources/db/migration/V*__*.sql

# Appliquer
./db-dev-tools.sh migrate
```

## 🚨 Bonnes Pratiques

### ✅ À FAIRE

- **Toujours générer** les migrations avant de modifier la BDD
- **Vérifier** le contenu des migrations générées
- **Tester** les migrations sur une copie des données
- **Committer** les migrations avec le code
- **Utiliser des noms descriptifs** pour les migrations

### ❌ À ÉVITER

- ⚠️ Modifier directement la BDD sans migration
- ⚠️ Éditer une migration déjà appliquée
- ⚠️ Supprimer des migrations existantes
- ⚠️ Oublier de committer les migrations

## 🛡️ Sécurité et Sauvegarde

### Environnement de Développement

```bash
# Sauvegarde avant reset
./db-dev-tools.sh info > migrations_backup.txt

# Reset complet si nécessaire
./db-dev-tools.sh reset
```

### Environnement de Production

- Les migrations sont **automatiquement appliquées** au démarrage
- **`flyway:clean` est désactivé** en production
- Utilisez `baseline` pour initialiser sur une BDD existante

## 🔧 Dépannage

### Erreur "Migration Checksum Mismatch"

```bash
# Réparer le schéma Flyway
./db-dev-tools.sh repair
```

### Base de Données Corrompue

```bash
# Reset complet (DÉVELOPPEMENT UNIQUEMENT)
./db-dev-tools.sh reset
```

### Migration Échouée

```bash
# Voir l'état détaillé
./db-dev-tools.sh info

# Réparer si nécessaire
./db-dev-tools.sh repair
```

## 📚 Ressources

- [Documentation Flyway](https://flywaydb.org/documentation/)
- [Guide Spring Boot + Flyway](https://spring.io/guides/gs/accessing-data-mysql/)
- [Hibernate Schema Generation](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#schema-generation)

## ⚡ Exemples Rapides

```bash
# Workflow complet nouvelle feature
git checkout -b feature/new-entity
# ... modifier les entités ...
./db-dev-tools.sh generate
./db-dev-tools.sh migrate
git add .
git commit -m "feat: add new entity with migration"

# Debug problème migration
./db-dev-tools.sh info
./db-dev-tools.sh validate

# Reset développement
./db-dev-tools.sh reset
```

---

> 💡 **Conseil** : Gardez toujours une sauvegarde de vos données importantes avant d'exécuter des migrations !
