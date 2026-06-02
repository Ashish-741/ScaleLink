# ==========================================
# Stage 1: Build the React Application
# ==========================================
FROM node:18-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and install dependencies
# We do this BEFORE copying the code to utilize Docker layer caching
COPY package*.json ./
RUN npm install

# Copy the rest of the frontend code
COPY . .

# Build the Vite project for production
# This generates the static files in the /app/dist folder
RUN npm run build

# ==========================================
# Stage 2: Serve with NGINX
# ==========================================
FROM nginx:alpine

# Copy the custom NGINX configuration to support React Router
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the static build files from Stage 1 to NGINX's serve directory
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80 for HTTP traffic
EXPOSE 80

# Start NGINX
CMD ["nginx", "-g", "daemon off;"]
