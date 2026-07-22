# Diagrammes UML — Traçabilité IA

Documentation visuelle alignée sur le code source du dépôt (`backend`, `frontend`, `ml-service`).

## Contenu

| Fichier | Description |
| --- | --- |
| [`use-case.puml`](use-case.puml) | Cas d’utilisation : acteurs métier et système (Utilisateur, Validateur, Administrateur, Auditeur, Service ML, Agents Groq). |
| [`sequence-creation-decision.puml`](sequence-creation-decision.puml) | Séquence de création d’une décision via `POST /api/decisions/analyze` (ML + SHAP, agents Groq, consensus, hashes, PostgreSQL). |
| [`sequence-validation-humaine.puml`](sequence-validation-humaine.puml) | Séquence de validation humaine : file `EN_ATTENTE`, consultation du dossier, approuver / rejeter / modifier / review. |
| [`class-diagram.puml`](class-diagram.puml) | Classes JPA principales du domaine décisionnel (`Decision`, `ExplanationFactor`, `ReponseAgentIA`, `ValidationAction`, etc.). |

Les SVG générés (si disponibles) se trouvent dans [`generated/`](generated/).

## Générer les SVG

Prérequis : [PlantUML](https://plantuml.com/) installé, ou un JAR PlantUML + Java.

```bash
plantuml -tsvg docs/uml/*.puml -o generated
```

Depuis le dossier `docs/uml` :

```bash
plantuml -tsvg *.puml -o generated
```

Avec Docker (sans installation globale) :

```bash
docker run --rm -v "%cd%/docs/uml:/data" plantuml/plantuml -tsvg /data/*.puml -o /data/generated
```

## Extension VS Code / Cursor recommandée

- **PlantUML** (jebbs / `jebbs.plantuml`) — prévisualisation et export des diagrammes.

## Maintenance

Mettre à jour ces diagrammes dès qu’un changement métier impacte :

- les acteurs ou rôles (`RoleEnum`) ;
- le flux de création / validation de décision ;
- les entités JPA ou leurs relations ;
- les intégrations ML ou Groq.

Ne pas y placer de secrets, clés API ou données personnelles.
