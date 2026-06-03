# ScaleLink Deployment Guide

This document outlines the deployment strategies for ScaleLink across different infrastructure environments.

## Option 1: PaaS + Serverless (Recommended)
This architecture leverages managed serverless databases and PaaS platforms for zero-maintenance operation.

### 1. Database (Neon.tech)
1. Create a PostgreSQL 15 instance on [Neon.tech](https://neon.tech).
2. Obtain the connection string (format: `postgresql://user:pass@host/db`).
3. Modify the protocol prefix to `jdbc:postgresql://` for Spring Boot compatibility.

### 2. Cache (Upstash)
1. Create a Redis instance on [Upstash](https://upstash.com).
2. Obtain the connection string, or note the host, port, and password.

### 3. Backend API (Render)
1. Create a new Web Service on [Render](https://render.com) pointing to the `scalelink-backend` directory.
2. Select the `Docker` environment.
3. Configure the following environment variables:
   - `SPRING_DATASOURCE_URL`: The Neon JDBC connection string.
   - `SPRING_DATA_REDIS_URL`: The Upstash connection string (or use HOST, PORT, PASSWORD individually).
   - `JWT_SECRET`: A 256-bit secure cryptographic key for token signing.
   - `APP_BASE_URL`: The public URL of this Render service (e.g., `https://api.domain.com`).

### 4. Frontend Client (Vercel)
1. Create a new project on [Vercel](https://vercel.com) pointing to the `scalelink-frontend` directory.
2. Set the framework preset to `Vite`.
3. Configure the following environment variable:
   - `VITE_API_BASE_URL`: The public URL of the backend API, suffixed with `/api/v1`.

---

## Option 2: Monolithic PaaS (Render)
This approach hosts all services within a single Render environment using internal networking.

1. Deploy PostgreSQL and Redis instances via the Render dashboard. Note their internal connection URLs.
2. Deploy the backend as a Docker Web Service, passing the internal database URLs as environment variables.
3. Deploy the frontend as a Static Site. Set the build command to `npm install && npm run build` and publish directory to `dist`. Configure a rewrite rule (`/*` -> `/index.html`) to support client-side routing.

---

## Option 3: VPS / IaaS (Docker Compose)
This approach deploys the application on a Virtual Private Server (e.g., AWS EC2, DigitalOcean Droplet) using Docker Compose.

1. Provision a Linux server (Ubuntu 22.04 LTS recommended) and install Docker and Docker Compose.
2. Clone the repository to the host machine.
3. Create a `.env` file in the project root:
   ```env
   POSTGRES_USER=scalelink
   POSTGRES_PASSWORD=<secure_password>
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=<secure_key>
   ```
4. Execute `docker-compose -f docker-compose.prod.yml up -d` to spin up the cluster.
5. Configure a reverse proxy (e.g., NGINX) and SSL termination on the host machine.
