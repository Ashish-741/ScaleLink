# ScaleLink Deployment Guide

Congratulations! Your application is fully dockerized and ready for production. Below are the three best ways to deploy this application.

---

## Option 1: The "Ultimate Permanent Free" Stack (Recommended)
*Best for: Portfolio projects. Maximizes performance and ensures your database never expires by combining the best free-tiers on the internet.*

### Step 1: Database (Neon.tech)
Neon is a serverless PostgreSQL platform. It gives you a permanent 500MB free database.
1. Go to [Neon.tech](https://neon.tech) and sign up with GitHub.
2. Create a new project (Name: `scalelink-db`, Postgres Version: 15).
3. Once created, copy the **Connection String** from the dashboard.
   - It looks like: `postgresql://neondb_owner:password@ep-cool-snowflake-123.us-east-2.aws.neon.tech/neondb?sslmode=require`
   - **Important:** Change `postgresql://` to `jdbc:postgresql://` for Spring Boot.

### Step 2: Cache (Upstash)
Upstash is a serverless Redis platform. It's completely free (up to 10k requests/day) and never expires.
1. Go to [Upstash.com](https://upstash.com) and sign up with GitHub.
2. Click **Create Database** under Redis. (Name: `scalelink-redis`, Type: Regional, uncheck TLS/SSL for simplicity, or leave checked if your Java client supports `rediss://`).
3. Scroll down to the **Java** section and copy the Hostname, Port, and Password. (Or just copy the Endpoint URL).

### Step 3: Backend (Render Web Service)
Render is the best place to host a free Dockerized Java Spring Boot application.
1. Go to [Render.com](https://render.com) and create a **Web Service**.
2. Connect your `ScaleLink` GitHub repository.
3. Configuration:
   - **Name:** `scalelink-backend`
   - **Root Directory:** `scalelink-backend`
   - **Environment:** `Docker`
   - **Instance Type:** `Free`
4. Add these Environment Variables:
   - `SPRING_DATASOURCE_URL` = Paste your Neon JDBC Connection String (from Step 1).
   - `SPRING_DATA_REDIS_HOST` = Paste your Upstash Endpoint / Hostname.
   - `SPRING_DATA_REDIS_PORT` = Paste your Upstash Port (usually `37397` or similar).
   - `SPRING_DATA_REDIS_PASSWORD` = Paste your Upstash Password.
   - `JWT_SECRET` = Make up a secure random string (e.g., `MySuperSecretKeyForJWTThatIsVeryLong123!`).
5. Click **Create**. Once it's Live, copy the public URL (e.g., `https://scalelink-backend.onrender.com`).

### Step 4: Frontend (Vercel)
Vercel is the gold standard for frontend hosting. It has a massive global CDN and is permanently free.
1. Go to [Vercel.com](https://vercel.com) and sign up with GitHub.
2. Click **Add New Project** and import your `ScaleLink` GitHub repository.
3. Configuration:
   - **Framework Preset:** `Vite`
   - **Root Directory:** Edit this and select `scalelink-frontend`.
4. Open the **Environment Variables** section and add:
   - **Name:** `VITE_API_BASE_URL`
   - **Value:** Paste your Render backend URL and add `/api/v1` to the end (e.g., `https://scalelink-backend.onrender.com/api/v1`).
5. Click **Deploy**. Vercel handles all SPA routing automatically!

---

## Option 2: Deploying entirely on Render (PaaS)
*Best for: Keeping everything under one single platform. Note that the free Postgres database expires after 90 days.*

1. **Database & Cache:** Create a free PostgreSQL and Redis instance on Render. Copy their Internal URLs.
2. **Backend:** Create a Web Service pointing to `scalelink-backend`. Use the `Docker` environment. Set `SPRING_DATASOURCE_URL` and `SPRING_DATA_REDIS_HOST` to the Internal URLs from step 1.
3. **Frontend:** Create a Static Site pointing to `scalelink-frontend`. Build command: `npm install && npm run build`. Publish directory: `dist`. Set `VITE_API_BASE_URL` to your Render backend URL. Add a Rewrite rule (`/*` to `/index.html`) in the Redirects tab.

---

## Option 3: Deploying to a VPS (AWS EC2 / DigitalOcean Droplet)
*Best for: Learning pure DevOps, having full control of the Linux environment, and running Docker Compose.*

1. Spin up a basic Linux server (Ubuntu 22.04 LTS).
2. Install Docker and Docker Compose (`sudo apt install docker.io docker-compose`).
3. Clone your repository: `git clone https://github.com/Ashish-741/ScaleLink.git`.
4. Create a `.env` file in the root directory:
   ```env
   POSTGRES_USER=scalelink
   POSTGRES_PASSWORD=your_secure_password
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=YourSecureKey123!
   ```
5. Run `sudo docker-compose -f docker-compose.prod.yml up -d`.
6. Configure NGINX and Let's Encrypt SSL on the host machine to point your domain to the Docker container.
