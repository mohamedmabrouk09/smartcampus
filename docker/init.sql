-- Initialisation de la base de données SmartCampus
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Comptes institutionnels (mot de passe : Passw0rd!2026)
INSERT INTO users (email, password) VALUES
    ('etudiant@usms.ac.ma', 'Passw0rd!2026')
ON CONFLICT (email) DO NOTHING;