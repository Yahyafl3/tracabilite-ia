# Répartition des tâches — Traçabilité IA

Projet en 3 parties : **backend** (Spring Boot), **frontend** (Angular), **ml-service** (Flask/scikit-learn).
Équipe : **A** = Yahya · **B** = binôme.

---

## Stack technique

- **Backend** : Spring Boot (Java 21) + PostgreSQL + Spring Security / JWT → *squelette à construire*.
- **Frontend** : Angular 21 (standalone components, SSR) → *landing + login faits*, à connecter au backend.
- **ml-service** : Flask + scikit-learn → *fonctionnel* (`/predict`, `/train`, `/domains`, `/health`), à enrichir.

---

## Règles de travail

1. **1 tâche = 1 responsable** : une seule personne code ET teste sa tâche avant de la partager.
2. **Auto-test obligatoire** avant de dire « fini » : test unitaire + test réel (Docker / navigateur / Postman), documenté.
3. **Rétro d'erreurs** après chaque tâche (voir modèle en bas).
4. **Rotation** : chacun touche backend + frontend + ml-service sur la durée du projet.

---

## Répartition équilibrée

Chaque personne a des tâches dans **les 3 dossiers**.

| #  | Tâche | Dossier | Responsable | Statut |
|----|-------|---------|-------------|--------|
| 1  | Entités JPA (Utilisateur, Agent, Decision, Validation, HashChain) → tables auto | backend | **A** | ⬜ À faire |
| 2  | Repositories (Spring Data JPA) | backend | **A** | ⬜ À faire |
| 3  | Auth JWT + endpoint `/api/auth/login` + seed d'un user admin | backend | **B** | ⬜ À faire |
| 4  | RBAC (rôles) + règles de sécurité par endpoint | backend | **B** | ⬜ À faire |
| 5  | Connexion login front → backend réel (token, `authGuard`, refresh) | frontend | **A** | ⬜ À faire |
| 6  | Page **Dashboard** (statistiques globales) | frontend | **A** | ⬜ À faire |
| 7  | Page **Décisions** (liste + détail + explicabilité) | frontend | **B** | ⬜ À faire |
| 8  | Page **Nouvelle décision** (formulaire → appel ML) | frontend | **B** | ⬜ À faire |
| 9  | Multi-modèles : plusieurs « agents IA » sur la même entrée | ml-service | **A** | ⬜ À faire |
| 10 | Endpoint `/compare` (compare les agents + scores) | ml-service | **A** | ⬜ À faire |
| 11 | `DecisionController` + `DecisionService` (CRUD décisions) | backend | **B** | ⬜ À faire |
| 12 | `MLDecisionService` : backend appelle `ml-service:5000/predict` | backend | **A** | ⬜ À faire |
| 13 | **Hash-chain** (audit trail immuable) | backend | **B** | ⬜ À faire |
| 14 | Endpoint backend `/api/comparaison` (classement des agents) | backend | **A** | ⬜ À faire |
| 15 | Page **Comparaison agents** (tableau + graphique %) | frontend | **B** | ⬜ À faire |
| 16 | Page **Validation humaine** (approuver/rejeter une décision IA) | frontend | **A** | ⬜ À faire |
| 17 | Explicabilité renforcée (facteurs / poids par feature) | ml-service | **B** | ⬜ À faire |
| 18 | Tests API ML + documentation des endpoints | ml-service | **B** | ⬜ À faire |

**Équilibre**

- **A** : backend (1, 2, 12, 14) · frontend (5, 6, 16) · ml-service (9, 10) → 9 tâches, 3 dossiers.
- **B** : backend (3, 4, 11, 13) · frontend (7, 8, 15) · ml-service (17, 18) → 9 tâches, 3 dossiers.

---

## Planning simple (4 sprints)

| Sprint | Objectif | A fait | B fait |
|--------|----------|--------|--------|
| **S1 – Fondations** | Base de données + authentification | Entités + Repos (1, 2) | Auth JWT + login (3, 4) |
| **S2 – Décisions** | Créer/lister des décisions + brancher le ML | ML backend (12) + connexion login front (5) | CRUD décisions (11) + page Décisions (7) |
| **S3 – IA & Comparaison** | Multi-agents + comparaison | Multi-modèles + `/compare` (9, 10) | Explicabilité + tests ML (17, 18) |
| **S4 – Finitions & Audit** | Dashboards + audit + validation | Dashboard (6) + endpoint comparaison (14) + validation (16) | Hash-chain (13) + page comparaison (15) + page nouvelle décision (8) |

---

## Tableau de suivi

Statuts : ⬜ À faire · 🔄 En cours · 🧪 En test · ✅ Fini · ⛔ Bloqué

| Tâche | Dossier | Responsable | Statut | Erreurs rencontrées | Solution |
|-------|---------|-------------|--------|---------------------|----------|
| Entités JPA | backend | A | ⬜ | — | — |
| Auth JWT | backend | B | ⬜ | — | — |
| … | … | … | … | … | … |

---

## Modèle de « rétro d'erreurs » (après chaque tâche)

```
### [Tâche] — [Responsable] — [Date]
- ✅ Ce qui a été fait :
- 🐞 Erreurs rencontrées :
- 🔧 Comment résolu :
- 💡 Conseil pour l'autre :
- 🧪 Comment j'ai testé : (unitaire + réel)
```
