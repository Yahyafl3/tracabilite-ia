# Présentation de Traçabilité IA

Traçabilité IA est une application web permettant de suivre, expliquer et auditer les décisions assistées par intelligence artificielle, en particulier l’analyse de demandes de crédit.

Pour chaque décision, le système conserve :
- le prompt et le contexte métier ;
- les données utilisées (`featuresJson`) ;
- la prédiction du modèle Machine Learning et le niveau de confiance ;
- les facteurs d’explication SHAP (`ExplanationFactor`) ;
- les réponses des agents IA Groq ;
- le consensus multi-agents ;
- les sources documentaires ;
- l’historique d’événements (`DecisionHistory`) ;
- les empreintes SHA-256 et le chaînage d’intégrité ;
- la validation humaine finale (`ValidationAction`).

L’objectif est de garantir la transparence, l’explicabilité, l’intégrité et la responsabilité humaine dans les décisions assistées par IA. Les agents Groq fournissent des recommandations complémentaires ; la décision finale reste toujours humaine.

## Technologies principales

- Angular 21 et PrimeNG (thème Aura / layout Sakai) pour le frontend ;
- Spring Boot 3.4 et Java 17 pour le backend ;
- PostgreSQL pour la base de données ;
- Python (Flask), Scikit-learn et SHAP pour le service Machine Learning ;
- Groq pour l’exécution des agents IA multi-modèles (OpenRouter conservé en option / historique) ;
- JWT pour l’authentification ;
- Docker Compose pour le déploiement local.

## Acteurs principaux

- **Utilisateur** : s’authentifie, crée et consulte les décisions (analyse crédit).
- **Validateur humain** : analyse le dossier complet (ML, SHAP, agents, sources) et prend la décision finale (approuver, rejeter, modifier, review).
- **Administrateur** : gère les utilisateurs, consulte l’audit et le statut des agents Groq.
- **Auditeur** : consulte l’audit des décisions et le résumé d’intégrité (rôle distinct dans l’application).
- **Service ML** : produit la prédiction et l’explication SHAP via un seul appel `/predict`.
- **Agents IA Groq** : fournissent des recommandations complémentaires et alimentent le consensus ; ils ne prennent pas la décision finale.

## Statuts de décision (réels)

`BROUILLON` → `EN_ATTENTE` → `APPROUVEE` | `MODIFIEE` | `REJETEE`

## Parcours type

1. Création d’une analyse crédit authentifiée (`POST /api/decisions/analyze`).
2. Prédiction ML + facteurs SHAP, sources par défaut, hashes métier.
3. Interrogation séquentielle des trois agents Groq et calcul du consensus (mode dégradé si Groq indisponible).
4. Passage en `EN_ATTENTE` et chaînage d’intégrité.
5. Validation humaine ultérieure sur le dossier complet.
