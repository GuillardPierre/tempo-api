-- Migration initiale pour Tempo API
-- Correspond aux entités JPA: User, Category, Worktime, WorktimeSeries, RecurrenceException, RefreshToken

-- Table des utilisateurs (correspond à @Table(name = "\"user\""))
CREATE TABLE IF NOT EXISTS public."user" (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_username UNIQUE (username)
);

-- Table des catégories (correspond à @Table(name = "category"))
CREATE TABLE IF NOT EXISTS public.category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE
);

-- Table des créneaux de travail (nom par défaut: worktime)
CREATE TABLE IF NOT EXISTS public.worktime (
    id SERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT false,
    user_id INTEGER NOT NULL,
    category_id INTEGER,  -- Optionnel selon l'entité Java
    CONSTRAINT fk_worktime_user FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_worktime_category FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE SET NULL
);

-- Table des séries récurrentes (nom par défaut: worktime_series)
CREATE TABLE IF NOT EXISTS public.worktime_series (
    id BIGSERIAL PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    recurrence VARCHAR(255),
    ignore_exceptions BOOLEAN DEFAULT false,
    user_id INTEGER NOT NULL,
    category_id INTEGER,  -- Optionnel selon l'entité Java
    CONSTRAINT fk_worktime_series_user FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_worktime_series_category FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE SET NULL
);

-- Table des exceptions de récurrence (nom par défaut: recurrence_exception)
CREATE TABLE IF NOT EXISTS public.recurrence_exception (
    id BIGSERIAL PRIMARY KEY,
    pause_start TIMESTAMP NOT NULL,
    pause_end TIMESTAMP NOT NULL
);

-- Table des tokens de rafraîchissement (nom par défaut: refresh_token)
CREATE TABLE IF NOT EXISTS public.refresh_token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL,  -- Taille augmentée pour les JWT
    expiry_date TIMESTAMP NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT uk_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE
);

-- Table de liaison pour les exceptions de récurrence
CREATE TABLE IF NOT EXISTS public.recurrence_exception_series (
    exception_id BIGINT NOT NULL,
    series_id BIGINT NOT NULL,
    PRIMARY KEY (exception_id, series_id),
    CONSTRAINT fk_exception_series_exception FOREIGN KEY (exception_id) REFERENCES public.recurrence_exception(id) ON DELETE CASCADE,
    CONSTRAINT fk_exception_series_series FOREIGN KEY (series_id) REFERENCES public.worktime_series(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_worktime_user_id ON public.worktime(user_id);
CREATE INDEX IF NOT EXISTS idx_worktime_category_id ON public.worktime(category_id);
CREATE INDEX IF NOT EXISTS idx_worktime_start_time ON public.worktime(start_time);
CREATE INDEX IF NOT EXISTS idx_worktime_series_user_id ON public.worktime_series(user_id);
CREATE INDEX IF NOT EXISTS idx_category_user_id ON public.category(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON public.refresh_token(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON public.refresh_token(expiry_date);

