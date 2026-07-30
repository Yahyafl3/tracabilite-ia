# Politique d’usage de l’IA

> Cadre d’usage des modèles ML et des agents LLM de Traçabilité IA.
> **Statut modèles : `DEMO_SYNTHETIC` — non validés pour décisions réelles.**

---

## 1. Principes transverses

- L’IA **assiste** ; elle ne remplace pas la responsabilité humaine du validateur domaine.
- Les agents génératifs (Groq / OpenRouter) sont **informatifs** : ils ne modifient ni la prédiction ML, ni la probabilité, ni les facteurs techniques, ni la décision humaine finale.
- Toute communication externe doit mentionner le caractère démonstratif / synthétique des scores.
- Pas d’entraînement furtif sur données utilisateurs réelles.

---

## 2. Domaine CREDIT

**Autorisé (démo / RC technique) :**

- Score de risque / défaut sur features déclaratives synthétiques ou de test
- Explications (importances / SHAP) pour pédagogie et traçabilité
- Comparaison avis agents vs score (informatif)

**Interdit :**

- Présenter le modèle comme système bancaire officiel marocain ou scorings réglementaire
- Décision de crédit exécutoire sans validation humaine et sans modèle validé sur données réelles
- Discrimination intentionnelle via variables proxies sensibles hors cadre légal

**Disclaimer à afficher / communiquer :**
*« Modèle DEMO_SYNTHETIC — score indicatif, non validé pour octroi de crédit réel. »*

---

## 3. Domaine MEDICAL

**Autorisé :**

- Estimation **indicative** de risque (ex. diabète synthétique) pour parcours de démo traçabilité
- Support à la discussion avec un professionnel de santé **dans un cadre de test**

**Interdit :**

- Diagnostic, prescription, priorisation clinique réelle
- Décision automatique de traitement ou d’orientation patient
- Envoi de dossiers médicaux réels vers LLM sans base légale

**Disclaimer :**
*« Sortie DEMO_SYNTHETIC — ne constitue pas un avis médical ni un diagnostic. Consulter un professionnel de santé. »*

---

## 4. Domaine EDUCATION

**Autorisé :**

- Signal d’aide à l’accompagnement pédagogique (risque décrochage synthétique)
- Orientation vers tutorat / suivi dans un scénario démo

**Interdit :**

- Sanction automatique, exclusion, notation officielle basée uniquement sur le score
- Décision administrative opposable sans revue humaine

**Disclaimer :**
*« Modèle DEMO_SYNTHETIC — indicateur d’accompagnement, non une décision disciplinaire ou d’orientation officielle. »*

---

## 5. LLM (agents)

- Utilisation : commentaires, contradictions apparentes, reformulations.
- Non-utilisation : recalcul du score, contournement des règles de validation.
- Minimiser les PII dans les prompts ; jamais de secrets.

---

## 6. Sanctions d’usage (organisationnelles)

Tout usage hors politique (ex. décision réelle sur DEMO_SYNTHETIC) = arrêt immédiat du flux concerné + incident + revue de gouvernance.
