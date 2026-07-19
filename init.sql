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

INSERT INTO public.employees (firstname, lastname, dateofbirth, gender, encryptedssn) VALUES
    ('Alice',   'Johnson', '1990-04-12', 'FEMALE',     '2g0G4PYuPs2EoQpzqjTWghKq2RijmhOO7wLXVuJW7jvbQV0JNjOH'),
    ('Michael', 'Smith',   '1985-09-23', 'MALE',       'iA/VYOqa9jmPviKr0YvNulFk6BaZbNoCmhwn9F64XvhdzRU/pMQi'),
    ('Jordan',  'Lee',     '1993-01-30', 'NON_BINARY', '66dl4k9+oGxRjXL2ixZrYE93FpJ2ghJlxNO/EcB25X43Mj3AUFcC'),
    ('Emily',   'Davis',   '1978-07-05', 'FEMALE',     'hfGgXZzbP9STeV6CYkQ8+GIFFl0zioqL2PB2kJx/0Maq6Fb9MuFH'),
    ('Chris',   'Taylor',  '1988-11-17', 'OTHER',      'lNKYuFauFkyfvTkENAW2SN/QE6dZWqxYkHuqDjIAvl/E4NFPO0z4');