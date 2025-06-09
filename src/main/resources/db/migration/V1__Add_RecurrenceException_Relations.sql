-- Création de la table recurrence_exception si elle n'existe pas
CREATE TABLE IF NOT EXISTS recurrence_exception (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pause_start DATETIME NOT NULL,
    pause_end DATETIME NOT NULL
);

-- Création de la table de jointure si elle n'existe pas
CREATE TABLE IF NOT EXISTS recurrence_exception_series (
    exception_id BIGINT NOT NULL,
    series_id BIGINT NOT NULL,
    PRIMARY KEY (exception_id, series_id),
    FOREIGN KEY (exception_id) REFERENCES recurrence_exception(id) ON DELETE CASCADE,
    FOREIGN KEY (series_id) REFERENCES worktime_series(id) ON DELETE CASCADE
);

-- Ajout de la colonne ignore_exceptions à worktime_series si elle n'existe pas
ALTER TABLE worktime_series
ADD COLUMN IF NOT EXISTS ignore_exceptions BOOLEAN DEFAULT false; 