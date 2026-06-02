# ScaleLink Deployment Guide

Congratulations! Your application is fully dockerized and ready for production. There are two common ways to deploy this application: using a **Virtual Private Server (VPS)** (like AWS EC2, DigitalOcean) or using a **Platform as a Service (PaaS)** (like Render, Railway).

Here is the step-by-step guide for both.

---

## Option 1: Deploying to a VPS (AWS EC2 / DigitalOcean Droplet)
*Best for: Learning DevOps, having full control, and running the exact `docker-compose.prod.yml` we built.*

### Prerequisites
1. Create an account on AWS, DigitalOcean, or Linode.
2. Spin up a basic Linux server (Ubuntu 22.04 LTS). A 1GB RAM / 1 CPU server is enough for a portfolio project.
3. SSH into your new server.

### Step-by-Step

**1. Install Docker & Docker Compose on the server**
```bash
sudo apt update
sudo apt install docker.io docker-compose -y
```

**2. Clone your repository**
```bash
git clone https://github.com/Ashish-741/ScaleLink.git
cd ScaleLink
```

**3. Set up Environment Variables**
Create a `.env` file in the root directory to hold your secrets securely (do NOT commit this file to GitHub).
```bash
nano .env
```
Paste the following (change the passwords for production!):
```env
# Database
POSTGRES_USER=scalelink
POSTGRES_PASSWORD=your_super_secret_db_password

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=MakeSureThisIsAtLeast256BitsLongAndVerySecure!!
```

**4. Start the Application!**
Run the orchestrator in detached mode (`-d`):
```bash
sudo docker-compose -f docker-compose.prod.yml up -d
```
Docker will pull the necessary images, build your Java and React containers, and start them. 

**5. Access your app**
Find your server's Public IP address and go to `http://YOUR_SERVER_IP` in your browser. NGINX will serve your React app, which will securely communicate with your backend container!

---

## Option 2: Deploying to Render (PaaS)
*Best for: Zero-maintenance, automatic deployments when you push to GitHub.*

Render doesn't use `docker-compose`; instead, you deploy each service individually and connect them via Render's internal private network.

### 1. Database & Cache
- In the Render Dashboard, create a **PostgreSQL** instance. (Save the "Internal Database URL").
- Create a **Redis** instance. (Save the "Internal Redis URL").

### 2. Spring Boot Backend
- Create a **Web Service** on Render and connect your GitHub repo.
- **Root Directory**: `scalelink-backend`
- **Environment**: `Docker` (Render will automatically detect your `Dockerfile`)
- **Environment Variables**:
  - `SPRING_DATASOURCE_URL`: The Internal DB URL from step 1 (e.g., `jdbc:postgresql://postgres-abc:5432/scalelink`)
  - `SPRING_DATA_REDIS_HOST`: The Internal Redis URL
  - `JWT_SECRET`: Your secure secret string.

### 3. React Frontend
- Create a **Static Site** on Render.
- **Root Directory**: `scalelink-frontend`
- **Build Command**: `npm install && npm run build`
- **Publish Directory**: `dist`
- **Redirects/Rewrites**: 
  - Since React Router is a Single Page Application, add a Rewrite rule in Render:
  - **Source**: `/*`
  - **Destination**: `/index.html`
  - **Action**: `Rewrite`

---

## 🔒 Post-Deployment Checklist
- [ ] **Buy a Domain**: Buy a domain name (like `scalelink.com`) and point its A-Record to your server's IP address.
- [ ] **SSL/HTTPS**: If using a VPS, install `certbot` to generate a free Let's Encrypt SSL certificate for your NGINX container. If using Render, SSL is handled automatically!
- [ ] **Update CORS**: In `CorsConfig.java`, update `allowedOrigins` to your new production domain instead of `localhost`.
