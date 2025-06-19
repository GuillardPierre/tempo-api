-- Migration pour supprimer la colonne is_active de la table worktime
-- Cette fonctionnalité a été retirée du modèle Worktime

-- Supprimer la colonne is_active de la table worktime
ALTER TABLE public.worktime DROP COLUMN IF EXISTS is_active; 