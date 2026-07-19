CREATE SCHEMA IF NOT EXISTS public;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender') THEN
        CREATE TYPE public.gender AS ENUM ('MALE', 'FEMALE', 'NON_BINARY', 'OTHER');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS public.employees (
    id              SERIAL PRIMARY KEY,
    firstname       VARCHAR(255) NOT NULL,
    lastname        VARCHAR(255) NOT NULL,
    dateofbirth     DATE NOT NULL,
    gender          public.gender NOT NULL,
    encryptedssn    TEXT NOT NULL
);
