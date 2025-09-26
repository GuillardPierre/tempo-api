-- Migration pour ajouter les colonnes exception_type et target_series_id à la table recurrence_exception

-- Ajouter la colonne exception_type avec une valeur par défaut
ALTER TABLE recurrence_exception 
ADD COLUMN exception_type VARCHAR(20) NOT NULL DEFAULT 'DAY';

-- Ajouter la colonne target_series_id (nullable pour les exceptions de type DAY)
ALTER TABLE recurrence_exception 
ADD COLUMN target_series_id BIGINT;

-- Ajouter une contrainte de clé étrangère pour target_series_id
ALTER TABLE recurrence_exception 
ADD CONSTRAINT fk_recurrence_exception_target_series 
FOREIGN KEY (target_series_id) REFERENCES worktime_series(id) ON DELETE CASCADE;

-- Ajouter un index pour améliorer les performances des requêtes
CREATE INDEX idx_recurrence_exception_type ON recurrence_exception(exception_type);
CREATE INDEX idx_recurrence_exception_target_series ON recurrence_exception(target_series_id);

-- Ajouter une contrainte pour s'assurer que target_series_id est défini uniquement pour les exceptions WORKTIME_SERIES
ALTER TABLE recurrence_exception 
ADD CONSTRAINT chk_exception_type_target_series 
CHECK (
    (exception_type = 'DAY' AND target_series_id IS NULL) OR 
    (exception_type = 'WORKTIME_SERIES' AND target_series_id IS NOT NULL)
);
