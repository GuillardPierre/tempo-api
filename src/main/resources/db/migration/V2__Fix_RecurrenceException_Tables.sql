-- Suppression des tables existantes
DROP TABLE IF EXISTS recurrence_exception_series;
DROP TABLE IF EXISTS recurrence_exception;

-- Recréation de la table recurrence_exception avec les contraintes NOT NULL
CREATE TABLE recurrence_exception (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pause_start DATETIME(6) NOT NULL,
    pause_end DATETIME(6) NOT NULL
);

-- Recréation de la table de jointure avec les bonnes contraintes
CREATE TABLE recurrence_exception_series (
    exception_id BIGINT NOT NULL,
    series_id BIGINT NOT NULL,
    PRIMARY KEY (exception_id, series_id),
    FOREIGN KEY (exception_id) REFERENCES recurrence_exception(id) ON DELETE CASCADE,
    FOREIGN KEY (series_id) REFERENCES worktime_series(id) ON DELETE CASCADE
); 