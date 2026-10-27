-- Initialisation complète de la base de données SmartCampus
-- Rôles: ETUDIANT, PROFESSEUR, ADMINISTRATEUR

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ETUDIANT','PROFESSEUR','ADMINISTRATEUR')),
    nom VARCHAR(100),
    prenom VARCHAR(100),
    filiere VARCHAR(100),
    groupe VARCHAR(50)
);

-- Cours
CREATE TABLE IF NOT EXISTS cours (
    id SERIAL PRIMARY KEY,
    intitule VARCHAR(150) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    filiere VARCHAR(100) NOT NULL,
    semestre VARCHAR(10) NOT NULL,
    prof_id INTEGER REFERENCES users(id) ON DELETE SET NULL
);

-- Emploi du temps
CREATE TABLE IF NOT EXISTS emploi_du_temps (
    id SERIAL PRIMARY KEY,
    cours_id INTEGER REFERENCES cours(id) ON DELETE CASCADE,
    jour VARCHAR(10) NOT NULL CHECK (jour IN ('LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI')),
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    salle VARCHAR(50),
    type_seance VARCHAR(10) CHECK (type_seance IN ('COURS','TD','TP'))
);

-- Examens
CREATE TABLE IF NOT EXISTS examens (
    id SERIAL PRIMARY KEY,
    cours_id INTEGER REFERENCES cours(id) ON DELETE CASCADE,
    filiere VARCHAR(100) NOT NULL,
    groupe VARCHAR(50),
    date_examen DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    salle VARCHAR(50),
    type_examen VARCHAR(15) CHECK (type_examen IN ('PARTIEL','CONTROLE','EXAMEN_FINAL'))
);

-- Notes
CREATE TABLE IF NOT EXISTS notes (
    id SERIAL PRIMARY KEY,
    etudiant_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    cours_id INTEGER REFERENCES cours(id) ON DELETE CASCADE,
    note_cc NUMERIC(4,2),
    note_tp NUMERIC(4,2),
    note_examen NUMERIC(4,2),
    note_finale NUMERIC(4,2),
    UNIQUE(etudiant_id, cours_id)
);

-- Réunions / Rendez-vous
CREATE TABLE IF NOT EXISTS reunions (
    id SERIAL PRIMARY KEY,
    prof_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    titre VARCHAR(150) NOT NULL,
    description TEXT,
    date_reunion DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    salle VARCHAR(50),
    participants VARCHAR(255)
);

-- Annonces
CREATE TABLE IF NOT EXISTS annonces (
    id SERIAL PRIMARY KEY,
    auteur_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    titre VARCHAR(150) NOT NULL,
    contenu TEXT NOT NULL,
    cible VARCHAR(20) CHECK (cible IN ('TOUS','ETUDIANT','PROFESSEUR','ADMINISTRATEUR')),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inscriptions (étudiants dans cours)
CREATE TABLE IF NOT EXISTS inscriptions (
    id SERIAL PRIMARY KEY,
    etudiant_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    cours_id INTEGER REFERENCES cours(id) ON DELETE CASCADE,
    UNIQUE(etudiant_id, cours_id)
);

-- ===== DONNÉES DE TEST =====

-- Utilisateurs
INSERT INTO users (email, password, role, nom, prenom, filiere, groupe) VALUES
('admin@usms.ac.ma', 'Passw0rd!2026', 'ADMINISTRATEUR', 'Benali', 'Karim', NULL, NULL),
('prof.ahmed@usms.ac.ma', 'Passw0rd!2026', 'PROFESSEUR', 'El Amrani', 'Ahmed', NULL, NULL),
('prof.sara@usms.ac.ma', 'Passw0rd!2026', 'PROFESSEUR', 'Bennis', 'Sara', NULL, NULL),
('etudiant@usms.ac.ma', 'Passw0rd!2026', 'ETUDIANT', 'Tazi', 'Youssef', 'Génie Informatique', 'GI3-A'),
('fatima@usms.ac.ma', 'Passw0rd!2026', 'ETUDIANT', 'Chakir', 'Fatima', 'Génie Informatique', 'GI3-A'),
('omar@usms.ac.ma', 'Passw0rd!2026', 'ETUDIANT', 'Idrissi', 'Omar', 'Réseaux & Télécom', 'RT3-B')
ON CONFLICT (email) DO NOTHING;

-- Cours
INSERT INTO cours (intitule, code, filiere, semestre, prof_id) VALUES
('Génie Logiciel', 'GL301', 'Génie Informatique', 'S5', (SELECT id FROM users WHERE email='prof.ahmed@usms.ac.ma')),
('Réseaux Avancés', 'RA301', 'Réseaux & Télécom', 'S5', (SELECT id FROM users WHERE email='prof.sara@usms.ac.ma')),
('Bases de Données Avancées', 'BDA301', 'Génie Informatique', 'S5', (SELECT id FROM users WHERE email='prof.ahmed@usms.ac.ma')),
('Programmation Web', 'PW301', 'Génie Informatique', 'S5', (SELECT id FROM users WHERE email='prof.sara@usms.ac.ma'))
ON CONFLICT (code) DO NOTHING;

-- Inscriptions
INSERT INTO inscriptions (etudiant_id, cours_id) VALUES
((SELECT id FROM users WHERE email='etudiant@usms.ac.ma'), (SELECT id FROM cours WHERE code='GL301')),
((SELECT id FROM users WHERE email='etudiant@usms.ac.ma'), (SELECT id FROM cours WHERE code='BDA301')),
((SELECT id FROM users WHERE email='etudiant@usms.ac.ma'), (SELECT id FROM cours WHERE code='PW301')),
((SELECT id FROM users WHERE email='fatima@usms.ac.ma'), (SELECT id FROM cours WHERE code='GL301')),
((SELECT id FROM users WHERE email='fatima@usms.ac.ma'), (SELECT id FROM cours WHERE code='BDA301')),
((SELECT id FROM users WHERE email='omar@usms.ac.ma'), (SELECT id FROM cours WHERE code='RA301'))
ON CONFLICT DO NOTHING;

-- Emploi du temps
INSERT INTO emploi_du_temps (cours_id, jour, heure_debut, heure_fin, salle, type_seance) VALUES
((SELECT id FROM cours WHERE code='GL301'), 'LUNDI', '08:30', '10:30', 'Amphi A', 'COURS'),
((SELECT id FROM cours WHERE code='GL301'), 'MARDI', '14:00', '16:00', 'TP-3', 'TP'),
((SELECT id FROM cours WHERE code='BDA301'), 'MERCREDI', '10:45', '12:45', 'Salle B12', 'COURS'),
((SELECT id FROM cours WHERE code='BDA301'), 'JEUDI', '08:30', '10:30', 'TD-2', 'TD'),
((SELECT id FROM cours WHERE code='PW301'), 'VENDREDI', '14:00', '17:00', 'TP-1', 'TP'),
((SELECT id FROM cours WHERE code='RA301'), 'LUNDI', '10:45', '12:45', 'Amphi B', 'COURS')
ON CONFLICT DO NOTHING;

-- Examens
INSERT INTO examens (cours_id, filiere, groupe, date_examen, heure_debut, heure_fin, salle, type_examen) VALUES
((SELECT id FROM cours WHERE code='GL301'), 'Génie Informatique', 'GI3-A', '2026-11-10', '09:00', '11:00', 'Amphi A', 'PARTIEL'),
((SELECT id FROM cours WHERE code='BDA301'), 'Génie Informatique', 'GI3-A', '2026-11-15', '09:00', '11:00', 'Amphi C', 'PARTIEL'),
((SELECT id FROM cours WHERE code='PW301'), 'Génie Informatique', 'GI3-A', '2026-11-22', '14:00', '16:00', 'Salle TP', 'PARTIEL'),
((SELECT id FROM cours WHERE code='RA301'), 'Réseaux & Télécom', 'RT3-B', '2026-11-12', '09:00', '11:00', 'Amphi B', 'PARTIEL')
ON CONFLICT DO NOTHING;

-- Notes
INSERT INTO notes (etudiant_id, cours_id, note_cc, note_tp, note_examen, note_finale) VALUES
((SELECT id FROM users WHERE email='etudiant@usms.ac.ma'), (SELECT id FROM cours WHERE code='GL301'), 15.5, 16.0, 14.0, 15.0),
((SELECT id FROM users WHERE email='etudiant@usms.ac.ma'), (SELECT id FROM cours WHERE code='BDA301'), 14.0, 17.0, 15.5, 15.5),
((SELECT id FROM users WHERE email='etudiant@usms.ac.ma'), (SELECT id FROM cours WHERE code='PW301'), 16.5, 18.0, 16.0, 16.8),
((SELECT id FROM users WHERE email='fatima@usms.ac.ma'), (SELECT id FROM cours WHERE code='GL301'), 17.0, 16.5, 17.5, 17.0)
ON CONFLICT (etudiant_id, cours_id) DO UPDATE SET
    note_cc = EXCLUDED.note_cc,
    note_tp = EXCLUDED.note_tp,
    note_examen = EXCLUDED.note_examen,
    note_finale = EXCLUDED.note_finale;

-- Réunions
INSERT INTO reunions (prof_id, titre, description, date_reunion, heure_debut, heure_fin, salle, participants) VALUES
((SELECT id FROM users WHERE email='prof.ahmed@usms.ac.ma'), 'Réunion Pédagogique', 'Bilan mi-semestre GI', '2026-11-05', '10:00', '12:00', 'Salle Conseil', 'Équipe Pédagogique GI'),
((SELECT id FROM users WHERE email='prof.sara@usms.ac.ma'), 'Soutien TP', 'Accompagnement projets Web', '2026-11-07', '15:00', '17:00', 'TP-1', 'Groupes GI3-A')
ON CONFLICT DO NOTHING;

-- Annonces
INSERT INTO annonces (auteur_id, titre, contenu, cible) VALUES
((SELECT id FROM users WHERE email='admin@usms.ac.ma'), 'Rentrée Partielle', 'Les partiels commencent le 10 Novembre 2026. Veuillez consulter vos emplois du temps.', 'TOUS'),
((SELECT id FROM users WHERE email='prof.ahmed@usms.ac.ma'), 'Rendu TP GL', 'Le TP n°3 de Génie Logiciel doit être rendu avant le 08/11/2026 sur la plateforme.', 'ETUDIANT')
ON CONFLICT DO NOTHING;
