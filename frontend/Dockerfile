# Stage 1: Build Angular SPA
FROM node:24-alpine AS build

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm ci --legacy-peer-deps

# Copy source
COPY . .

# Build the Angular SPA (outputs to dist/frontend/browser)
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy the built SPA
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copy custom Nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
