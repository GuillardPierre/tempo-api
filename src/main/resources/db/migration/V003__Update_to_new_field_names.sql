-- Migration pour adapter le schéma aux nouveaux noms de champs JPA
-- Date: 2025-07-23
-- Description: Renomme les colonnes pour correspondre aux nouveaux champs des entités

-- 1. Mettre à jour la table worktime
-- Renommer start_time -> start_hour et end_time -> end_hour
ALTER TABLE public.worktime RENAME COLUMN start_time TO start_hour;
ALTER TABLE public.worktime RENAME COLUMN end_time TO end_hour;

-- 2. Mettre à jour la table worktime_series
-- Renommer start_time -> start_hour et end_time -> end_hour
ALTER TABLE public.worktime_series RENAME COLUMN start_time TO start_hour;
ALTER TABLE public.worktime_series RENAME COLUMN end_time TO end_hour;

-- 3. Mettre à jour les index pour correspondre aux nouveaux noms de colonnes
DROP INDEX IF EXISTS idx_worktime_start_time;
DROP INDEX IF EXISTS idx_worktime_end;
CREATE INDEX IF NOT EXISTS idx_worktime_start_hour ON public.worktime(start_hour);
CREATE INDEX IF NOT EXISTS idx_worktime_end_hour ON public.worktime(end_hour);

-- 4. Ajouter des index sur les nouvelles colonnes de worktime_series
CREATE INDEX IF NOT EXISTS idx_worktime_series_start_date ON public.worktime_series(start_date);
CREATE INDEX IF NOT EXISTS idx_worktime_series_end_date ON public.worktime_series(end_date);
CREATE INDEX IF NOT EXISTS idx_worktime_series_start_hour ON public.worktime_series(start_hour);
CREATE INDEX IF NOT EXISTS idx_worktime_series_end_hour ON public.worktime_series(end_hour);