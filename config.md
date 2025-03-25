backend-app/
│── src/
│   ├── main/
│   │   ├── java/com/monprojet/
│   │   │   ├── config/        # 📌 Configurations (CORS, sécurité, etc.)
│   │   │   ├── controller/    # 📌 Endpoints API (REST Controllers)
│   │   │   ├── dto/           # 📌 Data Transfer Objects (Objets pour transmettre des données)
│   │   │   ├── entity/        # 📌 Entités JPA (Modèles de base de données)
│   │   │   ├── exception/     # 📌 Gestion des erreurs personnalisées
│   │   │   ├── repository/    # 📌 Interfaces pour gérer la base de données (Spring Data JPA)
│   │   │   ├── security/      # 📌 Classes de gestion de l'authentification et JWT
│   │   │   ├── service/       # 📌 Logique métier (implémentations des services)
│   │   │   ├── util/          # 📌 Fonctions utilitaires (hash de mot de passe, génération JWT...)
│   │   │   ├── BackendApplication.java  # 📌 Classe principale de l'application
│   │   ├── resources/
│   │   │   ├── static/        # 📌 Fichiers statiques (images, etc.)
│   │   │   ├── templates/     # 📌 Templates pour les emails, HTML, etc.
│   │   │   ├── application.properties  # 📌 Configuration principale
│   │   │   ├── application.yml         # 📌 Alternative en YAML
│── src/
│   ├── test/java/com/monprojet/        # 📌 Tests unitaires et d'intégration
│── pom.xml                              # 📌 Dépendances Maven
│── README.md                            # 📌 Documentation du projet
│── .gitignore                           # 📌 Exclusions Git
│── Dockerfile                           # 📌 Configuration pour Docker (optionnel)
│── docker-compose.yml                    # 📌 Déploiement avec Docker (optionnel)


### `./gradlew bootRun`
Cette commande exécute l'application Spring Boot en utilisant Gradle.  
- Elle compile le code si nécessaire.
- Elle démarre un serveur local pour tester l'application.  
- Utile pendant le développement pour voir les changements en direct.

---

### `./gradlew build`
Cette commande compile et assemble le projet.  
- Elle vérifie que le code est correct.
- Elle génère un fichier `.jar` ou `.war` dans `build/libs/`.  
- Elle exécute aussi les tests unitaires.

---

### `./gradlew build --refresh-dependencies`
Cette commande force Gradle à retélécharger toutes les dépendances du projet.  
- Utile si une dépendance a changé ou si le cache pose problème.  
- Permet d'éviter des erreurs liées aux versions obsolètes.
