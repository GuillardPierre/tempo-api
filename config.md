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

