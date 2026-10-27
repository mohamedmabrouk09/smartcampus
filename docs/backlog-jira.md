# Backlog Jira — SmartCampus (TP1)

Document de traçabilité du projet Jira SmartCampus (clé du projet : `SC`).

## Structure du backlog

### Epic AUTH — Authentification
Regroupe tous les besoins liés à la connexion des utilisateurs.

| Clé | Type | Titre | Priorité | Statut |
|-----|------|-------|----------|--------|
| SC-11 | Story | Se connecter avec e-mail et mot de passe | Haute | Done |
| SC-15 | Story | Se déconnecter | Moyenne | To Do |
| SC-16 | Story | Gérer les erreurs de connexion | Moyenne | To Do |
| SC-12 | Task | Préparer le modèle User | Haute | Done |
| SC-14 | Task | Écrire les tests du LoginService | Haute | Done |
| SC-13 | Bug | Message incorrect après trois échecs | Haute | Done |
| SC-17 | Sub-task | Ajouter les tests de LoginService | - | Done |

**Story SC-11 — critères d'acceptation :**
- Étant donné un compte actif, lorsque les identifiants sont corrects, alors l'accès est autorisé.
- Lorsque le mot de passe est incorrect, un message compréhensible est affiché.
- Après trois échecs consécutifs, l'événement est enregistré pour le suivi de sécurité.

### Epic ROOM — Gestion des salles
Consultation et recherche des salles disponibles.

| Clé | Type | Titre | Priorité | Statut |
|-----|------|-------|----------|--------|
| SC-21 | Story | Consulter la liste des salles | Moyenne | To Do |
| SC-22 | Story | Rechercher une salle par critères | Moyenne | To Do |
| SC-23 | Task | Créer le modèle Salle | - | To Do |

### Epic BOOK — Réservation
Création et annulation d'une réservation.

| Clé | Type | Titre | Priorité | Statut |
|-----|------|-------|----------|--------|
| SC-31 | Story | Réserver une salle | Haute | To Do |
| SC-32 | Story | Annuler une réservation | Moyenne | To Do |
| SC-33 | Task | Créer le modèle Reservation | - | To Do |

## Sprint 1 (réalisé)

Objectif : poser les bases de l'authentification.

| Élément | Clé | Workflow |
|---------|-----|----------|
| Story connexion | SC-11 | To Do → In Progress → Done |
| Task modèle User | SC-12 | To Do → In Progress → Done |
| Task tests | SC-14 | To Do → In Progress → Done |
| Bug message | SC-13 | To Do → In Progress → Done |

## Traçabilité Epic → Story → Git

```
Epic AUTH (SC-AUTH)
 └── Story SC-11 — Se connecter avec e-mail et mot de passe
      ├── Task SC-12 — Préparer le modèle User
      │    └── Git: feature/SC-11-login @ aa19f57 "SC-11 create User model"
      ├── Task SC-14 — Écrire les tests
      │    └── Git: feature/SC-11-login @ 8d205ac "SC-11: add authentication unit tests"
      └── Implementation
           ├── Git: feature/SC-11-login @ 980cd34 "SC-11 implement LoginService authentication"
           ├── Git: feature/SC-11-login @ 5234d60 "SC-11 add login demonstration"
           └── PR #2 → main (d7f4706)
```

Convention de nommage retenue pour les TP suivants :
- Branche : `feature/SC-11-login`
- Commit : `SC-11 <description>`
- Pull Request : `SC-11 — <titre>`

## Priorisation du Sprint 1

Justification : l'authentification est le socle fonctionnel sans lequel les épics ROOM et BOOK
ne peuvent exister (dépendance technique). SC-11 a été choisie comme premier user story car
elle livre une valeur utilisateurs visible rapidement et validable (critères d'acceptation
testables via LoginServiceTest). Le Bug SC-13 est inclus car il corrige un défaut bloquant
l'acceptation de la story.