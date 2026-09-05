# =============================================================================
# NexusCRM Frontend — Multi-stage Dockerfile
# =============================================================================
# Stage 1: Build the React app with Vite
# Stage 2: Serve with Nginx
# =============================================================================

# --- Stage 1: Build ---
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Set API URLs for production (Docker network)
ENV VITE_CRM_API_URL=http://localhost:8080/api/v1
ENV VITE_AI_API_URL=http://localhost:8000

RUN npm run build

# --- Stage 2: Serve with Nginx ---
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx config and content
RUN rm -rf ./*
RUN rm /etc/nginx/conf.d/default.conf

# Custom nginx config for SPA routing
COPY --from=build /app/nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets
COPY --from=build /app/dist .

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
