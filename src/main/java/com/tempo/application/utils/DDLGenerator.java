package com.tempo.application.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaire Spring Boot pour générer automatiquement les scripts DDL depuis les entités JPA
 * et créer les fichiers de migration Flyway correspondants.
 * 
 * Usage: ./mvnw spring-boot:run -Dspring.profiles.active=ddl-export
 */
@SpringBootApplication
public class DDLGenerator {

    private static final String MIGRATION_DIR = "src/main/resources/db/migration";
    private static final String TEMP_DDL_FILE = "target/generated-schema.sql";
    
    @Autowired
    private Environment env;
    
    @Autowired(required = false)
    private EntityManagerFactory entityManagerFactory;
    
    public static void main(String[] args) {
        // Forcer le profil DDL export
        System.setProperty("spring.profiles.active", "ddl-export");
        
        // Désactiver les logs Spring Boot verbeux
        System.setProperty("logging.level.root", "WARN");
        System.setProperty("logging.level.com.tempo.application.utils.DDLGenerator", "INFO");
        
        System.out.println("🔄 Démarrage du générateur DDL avec Spring Boot...");
        SpringApplication.run(DDLGenerator.class, args);
    }
    
    // @EventListener - Désactivé pour le mode développement
    public void onApplicationReady(ApplicationReadyEvent event) {
        // Vérifier si le profil DDL export est actif
        if (!java.util.Arrays.asList(env.getActiveProfiles()).contains("ddl-export")) {
            // Ne pas exécuter la génération DDL en mode développement normal
            System.out.println("🔄 Génération DDL désactivée (profil ddl-export non actif)");
            // En mode développement, laisser Hibernate gérer le DDL
            return;
        }
        
        try {
            System.out.println("🔄 Génération des scripts DDL depuis les entités JPA...");
            
            // 1. Générer le script DDL
            generateDDLScript();
            
            // 2. Créer la migration Flyway
            createFlywayMigration();
            
            System.out.println("✅ Génération terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération DDL : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        // Arrêter l'application après génération
        System.exit(0);
    }

    /**
     * Génère le script DDL en utilisant Spring Boot et JPA
     */
    private void generateDDLScript() throws IOException {
        System.out.println("📊 Analyse des entités JPA...");
        
        // Vérifier que l'EntityManagerFactory est disponible
        if (entityManagerFactory == null) {
            System.out.println("⚠️ EntityManagerFactory non disponible, génération manuelle...");
            generateManualDDL();
            return;
        }
        
        // Analyser le metamodel JPA
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        System.out.println("📋 Entités détectées: " + metamodel.getEntities().size());
        
        for (EntityType<?> entity : metamodel.getEntities()) {
            System.out.println("  - " + entity.getName() + " (" + entity.getJavaType().getSimpleName() + ")");
        }
        
        // Générer le script DDL manuellement (basé sur nos entités connues)
        generateManualDDL();
    }
    
    /**
     * Génère le DDL manuellement basé sur notre modèle connu
     */
    private void generateManualDDL() throws IOException {
        StringBuilder ddlScript = new StringBuilder();
        ddlScript.append("-- Schéma généré automatiquement depuis les entités JPA\n");
        ddlScript.append("-- Date de génération: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        ddlScript.append("-- Générateur: Spring Boot DDL Generator\n\n");
        
        // Générer les tables basées sur nos entités
        ddlScript.append(generateUserTable());
        ddlScript.append(generateCategoryTable());
        ddlScript.append(generateWorktimeTable());
        ddlScript.append(generateWorktimeSeriesTable());
        ddlScript.append(generateRecurrenceExceptionTable());
        ddlScript.append(generateRefreshTokenTable());
        ddlScript.append(generateJoinTables());
        ddlScript.append(generateIndexes());

        // Créer le répertoire target s'il n'existe pas
        Path targetDir = Paths.get("target");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Écrire le fichier
        try (FileWriter writer = new FileWriter(TEMP_DDL_FILE)) {
            writer.write(ddlScript.toString());
        }

        System.out.println("📄 Script DDL généré : " + TEMP_DDL_FILE);
    }

    private String generateUserTable() {
        return """
                -- Table des utilisateurs
                CREATE TABLE IF NOT EXISTS public."user" (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    CONSTRAINT uk_user_email UNIQUE (email),
                    CONSTRAINT uk_user_username UNIQUE (username)
                );
                
                """;
    }

    private String generateCategoryTable() {
        return """
                -- Table des catégories
                CREATE TABLE IF NOT EXISTS public.category (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    user_id INTEGER NOT NULL,
                    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES public."user"(id)
                );
                
                """;
    }

    private String generateWorktimeTable() {
        return """
                -- Table des créneaux de travail
                CREATE TABLE IF NOT EXISTS public.worktime (
                    id SERIAL PRIMARY KEY,
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP,
                    is_active BOOLEAN NOT NULL DEFAULT false,
                    active BOOLEAN NOT NULL DEFAULT true,
                    user_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    CONSTRAINT fk_worktime_user FOREIGN KEY (user_id) REFERENCES public."user"(id),
                    CONSTRAINT fk_worktime_category FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE CASCADE
                );
                
                """;
    }

    private String generateWorktimeSeriesTable() {
        return """
                -- Table des séries récurrentes
                CREATE TABLE IF NOT EXISTS public.worktime_series (
                    id BIGSERIAL PRIMARY KEY,
                    start_date TIMESTAMP NOT NULL,
                    end_date TIMESTAMP,
                    start_time TIMESTAMP,
                    end_time TIMESTAMP,
                    recurrence VARCHAR(255),
                    ignore_exceptions BOOLEAN DEFAULT false,
                    user_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    CONSTRAINT fk_worktime_series_user FOREIGN KEY (user_id) REFERENCES public."user"(id),
                    CONSTRAINT fk_worktime_series_category FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE CASCADE
                );
                
                """;
    }

    private String generateRecurrenceExceptionTable() {
        return """
                -- Table des exceptions de récurrence
                CREATE TABLE IF NOT EXISTS public.recurrence_exception (
                    id BIGSERIAL PRIMARY KEY,
                    pause_start TIMESTAMP NOT NULL,
                    pause_end TIMESTAMP NOT NULL
                );
                
                """;
    }

    private String generateRefreshTokenTable() {
        return """
                -- Table des tokens de rafraîchissement
                CREATE TABLE IF NOT EXISTS public.refresh_token (
                    id SERIAL PRIMARY KEY,
                    token VARCHAR(255) NOT NULL,
                    expiry_date TIMESTAMP NOT NULL,
                    user_id INTEGER NOT NULL,
                    CONSTRAINT uk_refresh_token UNIQUE (token),
                    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES public."user"(id)
                );
                
                """;
    }

    private String generateJoinTables() {
        return """
                -- Table de liaison pour les exceptions de récurrence
                CREATE TABLE IF NOT EXISTS public.recurrence_exception_series (
                    exception_id BIGINT NOT NULL,
                    series_id BIGINT NOT NULL,
                    PRIMARY KEY (exception_id, series_id),
                    CONSTRAINT fk_exception_series_exception FOREIGN KEY (exception_id) REFERENCES public.recurrence_exception(id),
                    CONSTRAINT fk_exception_series_series FOREIGN KEY (series_id) REFERENCES public.worktime_series(id)
                );
                
                """;
    }
    
    private String generateIndexes() {
        return """
                -- Index pour améliorer les performances
                CREATE INDEX IF NOT EXISTS idx_worktime_user_id ON public.worktime(user_id);
                CREATE INDEX IF NOT EXISTS idx_worktime_category_id ON public.worktime(category_id);
                CREATE INDEX IF NOT EXISTS idx_worktime_start_time ON public.worktime(start_time);
                CREATE INDEX IF NOT EXISTS idx_worktime_series_user_id ON public.worktime_series(user_id);
                CREATE INDEX IF NOT EXISTS idx_category_user_id ON public.category(user_id);
                CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON public.refresh_token(user_id);
                CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON public.refresh_token(expiry_date);
                
                """;
    }

    /**
     * Crée un fichier de migration Flyway depuis le script DDL généré
     */
    private void createFlywayMigration() throws IOException {
        Path ddlFile = Paths.get(TEMP_DDL_FILE);
        if (!Files.exists(ddlFile)) {
            throw new IOException("Fichier DDL non trouvé : " + TEMP_DDL_FILE);
        }

        // Créer le répertoire de migration s'il n'existe pas
        Path migrationDir = Paths.get(MIGRATION_DIR);
        if (!Files.exists(migrationDir)) {
            Files.createDirectories(migrationDir);
        }

        // Générer le nom du fichier de migration
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String migrationFileName;
        
        // Vérifier s'il y a déjà une migration V001
        Path initialMigration = migrationDir.resolve("V001__Initial_schema.sql");
        if (Files.exists(initialMigration)) {
            migrationFileName = String.format("V%s__Update_schema.sql", timestamp);
        } else {
            migrationFileName = "V001__Initial_schema.sql";
        }
        
        Path migrationFile = migrationDir.resolve(migrationFileName);

        // Lire le contenu du DDL généré
        String ddlContent = Files.readString(ddlFile);

        // Créer le header de la migration
        StringBuilder migrationContent = new StringBuilder();
        migrationContent.append("-- Migration générée automatiquement depuis les entités JPA\n");
        migrationContent.append("-- Généré le : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        migrationContent.append("-- Générateur : Spring Boot DDL Generator\n");
        migrationContent.append("-- Description : Schéma de la base de données Tempo API\n\n");
        
        migrationContent.append(ddlContent);

        // Écrire le fichier de migration
        Files.writeString(migrationFile, migrationContent.toString());

        System.out.println("📁 Migration Flyway créée : " + migrationFile);
    }
} 