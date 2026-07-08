# EXPLICATION DU PROJET - TRAÇABILITÉ IA

**Titre** : Système de Traçabilité des Décisions d'Intelligence Artificielle  
**Date** : 7 juillet 2026  
**Équipe** : Yahya Falhaoui & Badderdine Chourane

---

## 1. CONTEXTE ET PROBLÉMATIQUE

### Pourquoi ce projet ?

Avec l'utilisation croissante de l'Intelligence Artificielle dans les processus de décision (recrutement, crédit bancaire, diagnostic médical, etc.), il devient **crucial** de garantir :

1. **La transparence** : Savoir qui a pris quelle décision et pourquoi
2. **La traçabilité** : Pouvoir auditer toutes les décisions de l'IA
3. **La validation humaine** : S'assurer qu'un expert humain valide les décisions critiques
4. **L'intégrité** : Garantir qu'aucune décision ne peut être modifiée sans laisser de trace

### Le problème à résoudre

**Comment garantir que les décisions prises par une IA sont :**
- Tracées de manière infalsifiable
- Validées par des humains qualifiés
- Auditables à tout moment
- Protégées contre toute manipulation

---

## 2. SOLUTION PROPOSÉE

Une **application web complète** qui :

1. **Enregistre** toutes les décisions prises par l'IA
2. **Chaîne cryptographiquement** les décisions (comme une blockchain simplifiée)
3. **Permet aux validateurs humains** d'approuver, rejeter ou modifier les décisions
4. **Trace toutes les actions** (qui a fait quoi, quand, pourquoi)
5. **Fournit un tableau de bord** avec statistiques et historique complet

---

## 3. ACTEURS DU SYSTÈME

Le système comporte **4 types d'utilisateurs** avec des rôles différents :

### 1. ADMINISTRATEUR
**Rôle** : Gérer le système et les utilisateurs

**Responsabilités** :
- Créer et gérer les comptes utilisateurs
- Attribuer les rôles (VALIDATEUR, SYSTEME_IA, AUDITEUR)
- Configurer le système
- Superviser l'ensemble des opérations

**Permissions** :
- Accès complet au système
- Gestion des utilisateurs
- Consultation de toutes les données

### 2. SYSTÈME IA
**Rôle** : Créer des décisions automatiquement

**Responsabilités** :
- Générer des décisions basées sur des algorithmes d'IA
- Fournir un contexte et un score de confiance pour chaque décision
- Enregistrer chaque décision dans le système avec un hash cryptographique

**Permissions** :
- Créer des décisions uniquement
- Consulter ses propres décisions
- Pas de validation ni de modification

### 3. VALIDATEUR
**Rôle** : Valider ou rejeter les décisions de l'IA

**Responsabilités** :
- Examiner les décisions en attente
- Approuver les décisions correctes
- Rejeter les décisions incorrectes (avec commentaire obligatoire)
- Modifier les décisions si nécessaire (avec justification)

**Permissions** :
- Consulter les décisions en attente
- Approuver/Rejeter/Modifier avec commentaire
- Voir l'historique de ses validations

### 4. AUDITEUR
**Rôle** : Consulter et auditer le système

**Responsabilités** :
- Vérifier l'intégrité du chaînage cryptographique
- Consulter l'historique complet des décisions
- Générer des rapports d'audit
- Identifier les anomalies

**Permissions** :
- Lecture seule sur toutes les données
- Vérification de l'intégrité des hashes
- Génération de rapports

---

## 4. FONCTIONNALITÉS PRINCIPALES

### A. GESTION DES DÉCISIONS

**1. Création d'une décision (par l'IA)**
```
Entrées :
- Contenu de la décision (texte)
- Contexte (pourquoi cette décision)
- Score de confiance (0-100%)

Processus :
1. Le système IA crée une décision
2. Un hash SHA-256 est calculé (contenu + contexte + timestamp + hash précédent)
3. La décision est enregistrée avec statut "EN_ATTENTE"
4. Elle est liée à la décision précédente (chaînage)

Résultat :
- Décision enregistrée de manière infalsifiable
- Notification envoyée aux validateurs
```

**2. Validation d'une décision (par un Validateur)**
```
Actions possibles :
- APPROUVER : La décision est correcte
- REJETER : La décision est incorrecte (commentaire obligatoire)
- MODIFIER : La décision nécessite des ajustements (justification obligatoire)

Processus :
1. Le validateur examine la décision et son contexte
2. Il choisit une action et fournit un commentaire
3. L'action est enregistrée avec horodatage
4. Un hash de l'action est calculé pour traçabilité
5. Le statut de la décision est mis à jour

Résultat :
- Action tracée de manière permanente
- Décision avec nouveau statut (APPROUVE, REJETE, MODIFIE)
```

### B. CHAÎNAGE CRYPTOGRAPHIQUE (Blockchain simplifiée)

**Principe** : Chaque décision est liée à la précédente par un hash

**Comment ça marche ?**

```
Décision 1 :
- Contenu : "Approuver le prêt de Jean Dupont"
- Hash précédent : null (première décision)
- Hash courant : SHA256(contenu + timestamp + null)
- Hash courant = "a3f5b2..."

Décision 2 :
- Contenu : "Rejeter le prêt de Marie Martin"
- Hash précédent : "a3f5b2..." (hash de la décision 1)
- Hash courant : SHA256(contenu + timestamp + "a3f5b2...")
- Hash courant = "7c8d9e..."

Décision 3 :
- Contenu : "Approuver le crédit de Paul Durand"
- Hash précédent : "7c8d9e..." (hash de la décision 2)
- Hash courant : SHA256(contenu + timestamp + "7c8d9e...")
- Hash courant = "2b4f8a..."
```

**Pourquoi c'est important ?**

Si quelqu'un essaie de modifier la Décision 2 :
- Son hash va changer : "7c8d9e..." devient "9f3a1b..."
- Mais la Décision 3 référence toujours l'ancien hash "7c8d9e..."
- **La chaîne est brisée** → Détection immédiate de la manipulation !

### C. AUDIT TRAIL (Traçabilité complète)

**Chaque action est enregistrée** :
```
Exemple d'historique pour une décision :

[2026-07-08 10:00] - SystemeIA #45 crée une décision
  → Contenu : "Approuver prêt 50000€"
  → Statut : EN_ATTENTE
  → Hash : a3f5b2...

[2026-07-08 14:30] - Validateur "Marie Durand" examine la décision
  → Action : APPROUVER
  → Commentaire : "Tous les critères sont respectés"
  → Hash action : 8d9c3f...

[2026-07-08 14:31] - Système met à jour le statut
  → Nouveau statut : APPROUVE
```

**Toutes les actions sont stockées avec** :
- Qui (utilisateur)
- Quoi (action)
- Quand (date et heure exacte)
- Pourquoi (commentaire)
- Hash cryptographique (preuve d'intégrité)

### D. TABLEAU DE BORD

**Statistiques en temps réel** :
- Nombre total de décisions
- Décisions en attente de validation
- Taux d'approbation / rejet
- Temps moyen de validation
- Décisions par système IA
- Validations par validateur

**Graphiques** :
- Évolution des décisions dans le temps
- Répartition par statut (EN_ATTENTE, APPROUVE, REJETE, MODIFIE)
- Performance des validateurs
- Score de confiance moyen de l'IA

---

## 5. ARCHITECTURE TECHNIQUE

### BACKEND (Spring Boot)

```
Architecture en couches :

┌─────────────────────────────────────┐
│      Controllers (API REST)         │  ← Interface HTTP
├─────────────────────────────────────┤
│           Services                  │  ← Logique métier
├─────────────────────────────────────┤
│         Repositories                │  ← Accès base de données
├─────────────────────────────────────┤
│      Entities (JPA)                 │  ← Modèles de données
└─────────────────────────────────────┘
           ↓
    PostgreSQL Database
```

**Endpoints API principaux** :

```
AUTHENTIFICATION :
POST   /api/auth/login          → Connexion
POST   /api/auth/register       → Inscription

DÉCISIONS :
GET    /api/decisions           → Liste des décisions
GET    /api/decisions/{id}      → Détail d'une décision
POST   /api/decisions           → Créer une décision (IA)
PUT    /api/decisions/{id}      → Modifier une décision

VALIDATION :
POST   /api/validations         → Valider une décision
GET    /api/validations/{id}    → Historique de validation

DASHBOARD :
GET    /api/dashboard/stats     → Statistiques globales

AUDIT :
GET    /api/audit               → Journal d'audit complet
GET    /api/audit/verify        → Vérifier intégrité chaîne
```

### FRONTEND (React)

```
Structure des pages :

┌─────────────────────────────────────┐
│        Page de connexion            │
└─────────────────────────────────────┘
           ↓ (après login)
┌─────────────────────────────────────┐
│          Dashboard                  │  ← Vue d'ensemble
│  - Statistiques                     │
│  - Graphiques                       │
│  - Alertes                          │
└─────────────────────────────────────┘
           ↓
     ┌──────────┬──────────┬──────────┐
     ↓          ↓          ↓          ↓
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│Décisions│ │Validation│ │ Audit  │ │ Admin  │
│  - Liste│ │  - En    │ │ - Logs │ │ - Users│
│  - Créer│ │  attente │ │ - Stats│ │ - Config│
│  - Détail│ │  - Valider│ │ - Verify│ │       │
└─────────┘ └─────────┘ └─────────┘ └─────────┘
```

### BASE DE DONNÉES (PostgreSQL)

**Tables principales** :

```sql
Table : utilisateurs
- id (PK)
- email (unique)
- mot_de_passe (hashé)
- nom, prenom
- role (ADMIN, VALIDATEUR, SYSTEME_IA, AUDITEUR)
- type_utilisateur (discriminateur pour héritage)
- actif (boolean)
- date_creation

Table : decisions
- id (PK)
- contenu (TEXT)
- contexte (TEXT)
- statut (EN_ATTENTE, APPROUVE, REJETE, MODIFIE)
- systeme_ia_id (FK → utilisateurs)
- date_creation
- hash_courant (unique)
- hash_precedent
- decision_precedente_id (FK → decisions) ← Chaînage !
- score_confiance

Table : validation_actions
- id (PK)
- decision_id (FK → decisions)
- validateur_id (FK → utilisateurs)
- type_action (APPROUVER, REJETER, MODIFIER)
- commentaire (TEXT, obligatoire)
- date_action
- hash_action
```

**Relations** :
- Un Système IA peut créer plusieurs Décisions (1:N)
- Une Décision peut avoir plusieurs Actions de Validation (1:N)
- Une Décision référence la Décision précédente (auto-référence)

---

## 6. SÉCURITÉ

### AUTHENTIFICATION JWT (JSON Web Token)

**Processus de connexion** :
```
1. Utilisateur envoie : email + mot_de_passe
2. Backend vérifie les identifiants
3. Si OK : Génère un token JWT signé
4. Token contient : id, email, role, expiration
5. Frontend stocke le token
6. Chaque requête inclut le token dans le header
7. Backend vérifie la signature du token
```

### CONTRÔLE D'ACCÈS (RBAC - Role Based Access Control)

```
ADMIN peut :
- Tout faire (CRUD utilisateurs, consultation complète)

SYSTEME_IA peut :
- Créer des décisions
- Consulter ses propres décisions

VALIDATEUR peut :
- Consulter les décisions EN_ATTENTE
- Approuver/Rejeter/Modifier avec commentaire
- Voir l'historique de ses validations

AUDITEUR peut :
- Consultation en lecture seule
- Vérification de l'intégrité
- Génération de rapports
```

### PROTECTION DES DONNÉES

- Mots de passe hashés avec BCrypt (jamais stockés en clair)
- Communication HTTPS en production
- Tokens JWT signés avec clé secrète
- Validation des entrées (éviter injection SQL)
- Protection CORS configurée

---

## 7. CHAÎNAGE CRYPTOGRAPHIQUE DÉTAILLÉ

### Algorithme de hachage (SHA-256)

**Implémentation** :

```java
public class HashUtils {
    
    public static String calculateHash(Decision decision) {
        String data = decision.getContenu() + 
                     decision.getContexte() + 
                     decision.getDateCreation() + 
                     decision.getHashPrecedent();
        
        return SHA256(data);
    }
    
    public static boolean verifyChain(List<Decision> decisions) {
        for (int i = 1; i < decisions.size(); i++) {
            Decision current = decisions.get(i);
            Decision previous = decisions.get(i - 1);
            
            // Vérifier que le hash précédent correspond
            if (!current.getHashPrecedent().equals(previous.getHashCourant())) {
                return false; // Chaîne brisée !
            }
            
            // Recalculer le hash et vérifier
            String calculatedHash = calculateHash(current);
            if (!calculatedHash.equals(current.getHashCourant())) {
                return false; // Hash modifié !
            }
        }
        return true; // Chaîne intègre
    }
}
```

### Avantages du chaînage

1. **Détection immédiate** de toute modification
2. **Preuve d'ordre chronologique** : Impossible de réordonner les décisions
3. **Immutabilité** : Une fois créée, une décision ne peut être modifiée sans briser la chaîne
4. **Audit simplifié** : Vérification automatique de l'intégrité

---

## 8. CAS D'UTILISATION CONCRETS

### Cas 1 : Crédit bancaire

```
1. Système IA analyse la demande de crédit de Jean Dupont
   → Score de confiance : 85%
   → Décision : "APPROUVER crédit 50000€"
   → Contexte : "Salaire stable, historique positif"

2. La décision est enregistrée avec statut EN_ATTENTE

3. Validateur (expert bancaire) Marie Durand examine la décision
   → Vérifie les documents
   → Confirme l'analyse de l'IA
   → Action : APPROUVER
   → Commentaire : "Dossier complet et conforme"

4. Décision devient APPROUVE
   → Jean Dupont peut obtenir son crédit
   → Toute l'historique est tracé et auditable
```

### Cas 2 : Rejet avec modification

```
1. Système IA analyse la candidature de Marie Martin
   → Score de confiance : 65%
   → Décision : "REJETER candidature"
   → Contexte : "Expérience insuffisante"

2. Validateur (RH) Paul Laurent examine la décision
   → Remarque : L'IA n'a pas considéré les certifications
   → Action : MODIFIER
   → Commentaire : "Candidature acceptée : certifications compensent l'expérience"

3. Décision devient MODIFIE
   → Marie Martin est convoquée pour un entretien
   → L'action du validateur est tracée avec justification
```

---

## 9. BÉNÉFICES DU SYSTÈME

### Pour l'organisation

1. **Conformité réglementaire** : Traçabilité complète (RGPD, ISO)
2. **Réduction des risques** : Validation humaine systématique
3. **Amélioration continue** : Analyse des décisions pour affiner l'IA
4. **Protection juridique** : Preuve de chaque décision et validation

### Pour les utilisateurs

1. **Transparence** : Comprendre pourquoi une décision a été prise
2. **Équité** : Validation humaine garantit l'absence de biais
3. **Recours** : Possibilité de contester une décision avec audit trail
4. **Confiance** : Système infalsifiable et auditable

### Pour les auditeurs

1. **Audit facilité** : Toutes les données en un seul endroit
2. **Vérification automatique** : Intégrité du chaînage
3. **Rapports détaillés** : Statistiques et historiques complets
4. **Détection d'anomalies** : Alertes automatiques

---

## 10. TECHNOLOGIES UTILISÉES

### Backend
- **Java 21** : Langage principal
- **Spring Boot 3.4.1** : Framework web
- **Spring Security** : Authentification et autorisation
- **Spring Data JPA** : Accès base de données
- **PostgreSQL 16** : Base de données relationnelle
- **JWT** : Tokens d'authentification
- **Maven** : Gestion des dépendances
- **Docker** : Conteneurisation

### Frontend
- **React 18** : Framework JavaScript
- **Vite** : Build tool moderne et rapide
- **Axios** : Client HTTP
- **React Router** : Navigation
- **CSS3** : Styles

### Infrastructure
- **Docker Compose** : Orchestration des conteneurs
- **Git** : Contrôle de version
- **GitHub** : Hébergement du code

---

## 11. ÉVOLUTIONS FUTURES POSSIBLES

1. **Notifications en temps réel** (WebSocket)
2. **Intégration avec plusieurs systèmes IA**
3. **Export des rapports en PDF**
4. **Tableau de bord analytique avancé**
5. **API publique pour intégration externe**
6. **Machine Learning pour détection d'anomalies**
7. **Application mobile**
8. **Blockchain complète** (au lieu du chaînage simplifié)

---

## CONCLUSION

Ce projet démontre une solution moderne et sécurisée pour garantir :
- La **traçabilité** complète des décisions IA
- La **validation humaine** systématique
- L'**intégrité** des données grâce au chaînage cryptographique
- La **conformité** aux réglementations

C'est un système essentiel dans un contexte où l'IA prend des décisions de plus en plus critiques et où la transparence devient une exigence légale et éthique.

---

**Document rédigé le 7 juillet 2026**  
**Projet Fin d'Études - Traçabilité IA**
