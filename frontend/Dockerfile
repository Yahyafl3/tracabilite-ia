# Image Node.js pour le développement
FROM node:20-alpine

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./

# Installer les dépendances
RUN npm install

# Copier tout le code source
COPY . .

# Exposer le port Vite
EXPOSE 5173

# Lancer le serveur de développement avec hot reload
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
