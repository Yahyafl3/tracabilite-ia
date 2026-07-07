# Tracabilité IA - Système de Traçabilité des Décisions Assistées par IA

## 🚀 Lancement Rapide avec Docker

### Prérequis
- Docker Desktop installé et démarré
- Aucune autre installation nécessaire ! 🎉

### Démarrage du Projet

**Option 1 : Via Docker Desktop (Recommandé)**
1. Ouvrez Docker Desktop
2. Cliquez sur "Compose" dans le menu
3. Importez le fichier `docker-compose.yml` depuis le dossier du projet
4. Cliquez sur "Start" / "Démarrer"

**Option 2 : Via la ligne de commande**
```bash
# À la racine du projet (où se trouve docker-compose.yml)
docker-compose up
```

### 🌐 Accès aux Services

Une fois les conteneurs démarrés :
- **Frontend React** : http://localhost:5173
- **Backend Spring Boot** : http://localhost:8080
- **Base de données PostgreSQL** : localhost:5432

### ✨ Fonctionnalités Automatiques

✅ Installation automatique des packages backend (Maven)
✅ Installation automatique des packages frontend (npm)
✅ Configuration automatique de la base de données PostgreSQL
✅ Hot reload activé : vos modifications de code sont prises en compte automatiquement
✅ Synchronisation entre vous et votre binôme

### 🔄 Workflow de Développement

1. **Modifier le code** dans VSCode ou votre éditeur préféré
2. **Sauvegardez** - les changements sont automatiquement détectés
3. **Rechargez** le navigateur pour le frontend ou redémarrez le backend si nécessaire

#### Backend (Spring Boot)
- Les modifications de code nécessitent un redémarrage du conteneur backend
- Dans Docker Desktop : cliquez sur "Restart" pour le conteneur `tracabilite-backend`

#### Frontend (React + Vite)
- Hot Module Replacement (HMR) activé
- Pas besoin de redémarrer, le navigateur se met à jour automatiquement !

### 📦 Synchronisation avec votre Binôme

Quand votre binôme ou vous ajoutez des packages :

**Frontend (nouvelle dépendance npm)**
```bash
# Après un git pull, reconstruisez le conteneur frontend
docker-compose up --build frontend
```

**Backend (nouvelle dépendance Maven)**
```bash
# Après un git pull, reconstruisez le conteneur backend
docker-compose up --build backend
```

### 🛑 Arrêter le Projet

**Via Docker Desktop**
- Cliquez sur "Stop" dans l'interface

**Via ligne de commande**
```bash
docker-compose down
```

### 🗑️ Réinitialiser Complètement (si problème)

```bash
# Arrêter et supprimer tous les conteneurs + volumes
docker-compose down -v

# Reconstruire et redémarrer
docker-compose up --build
```

### 📊 Structure du Projet

```
tracabilite-ia/
├── backend/              # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/             # React + Vite
│   ├── src/
│   ├── package.json
│   └── Dockerfile
└── docker-compose.yml    # Configuration Docker
```

### 🔧 Configuration de la Base de Données

**Identifiants PostgreSQL** (déjà configurés dans docker-compose.yml) :
- Database : `tracabilite_ia`
- Username : `tracabilite`
- Password : `tracabilite123`
- Port : `5432`

### ❓ Problèmes Courants

**Le port 5173 ou 8080 est déjà utilisé**
- Arrêtez l'application qui utilise ce port
- Ou modifiez les ports dans `docker-compose.yml`

**Les changements ne sont pas détectés**
- Vérifiez que les volumes sont bien montés dans docker-compose.yml
- Redémarrez Docker Desktop

**Erreur de connexion à la base de données**
- Attendez quelques secondes que PostgreSQL soit complètement démarré
- Vérifiez que le conteneur `tracabilite-postgres` est en état "healthy"

### 🎯 Avantages de cette Configuration

✨ **Zéro installation locale** : Pas besoin d'installer Java, Maven, Node.js, PostgreSQL
✨ **Environnement identique** : Vous et votre binôme avez exactement la même configuration
✨ **Isolation** : Le projet ne pollue pas votre système
✨ **Facile à partager** : Un simple `git clone` + `docker-compose up` et c'est parti !

### 📝 Notes de Développement

- Ne committez **jamais** les fichiers `node_modules/` ou `target/`
- Les `.dockerignore` sont déjà configurés pour éviter ça
- La base de données est persistante (volume Docker) même après redémarrage

---

**Bon développement ! 🚀**
