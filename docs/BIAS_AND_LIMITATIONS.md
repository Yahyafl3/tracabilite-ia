# Biais et limites

> Synthèse opérationnelle. Détails datasets : `DATASETS.md`, `LIMITATIONS.md`.
> **Tous les modèles = `DEMO_SYNTHETIC`.**

---

## 1. Limite principale

Les performances reportées (recall, F1, ROC-AUC) sont mesurées sur **données synthétiques**. Elles **ne prédisent pas** la performance sur un portefeuille, une cohorte clinique ou une population étudiante réelle.

---

## 2. Sources de biais possibles

| Source | Risque |
|--------|--------|
| Génération synthétique | Corrélations artificielles, distributions irréalistes |
| Sous-représentation régions / profils | Erreurs systématiques sur certains groupes |
| Proxy sensibles (région, secteur, etc.) | Discrimination indirecte si usage réel |
| Choix de la métrique de sélection | Favorise un type d’erreur (ex. recall) |
| Agents LLM | Hallucinations, biais culturels dans le texte — **sans** modifier le score ML |
| Validation humaine | Biais confirmatoires ; d’où traçabilité des désaccords |

---

## 3. Limites par domaine

### CREDIT

- Pas un modèle réglementaire / bancaire officiel.
- Features financières simplifiées vs dossier réel.
- Faux négatifs / positifs de défaut ont un coût asymétrique **non calibré métier**.

### MEDICAL

- Indicateur non clinique.
- Risque éthique élevé si interprété comme diagnostic.
- Données santé = classification maximale même en démo peuplée.

### EDUCATION

- Risque de stigmatisation si score exposé sans accompagnement.
- Pas un outil disciplinaire.

---

## 4. Ce que la plateforme trace (et ce qu’elle ne garantit pas)

**Trace :** inputs, score, facteurs, avis agents, décision humaine, hash snapshot.

**Ne garantit pas :** équité demographic parity, absence de biais, conformité réglementaire métier, exactitude clinique/bancaire.

---

## 5. Mitigations actuelles / cibles

| Mitigation | État |
|------------|------|
| Disclaimer DEMO_SYNTHETIC | À appliquer partout (docs + UI) |
| Human oversight | Implémenté (files de validation) |
| Datasets card / docs | Partiel (`DATASET_CARD.md`) |
| Monitoring dérive | Documenté, outillage **planifié** |
| Fairness tests groupes | **Non implémenté** |
| Réentraînement données réelles anonymisées | **Hors scope** tant que DEMO |

---

## 6. Communication obligatoire

Toute démo, soutenance ou doc externe doit indiquer clairement :

1. Données synthétiques
2. Non validation pour décisions réelles
3. Supervision humaine requise

Omettre ces points = non-conformité à `AI_USAGE_POLICY.md`.
