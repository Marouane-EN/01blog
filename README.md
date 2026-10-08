# 01Blog

A full-stack social blogging platform. Users publish posts with images and videos, follow each other, like and comment, and report content. Administrators moderate everything from a dedicated dashboard.

- **Backend:** Spring Boot REST API (JWT + Google/GitHub OAuth2 login)
- **Frontend:** Angular single-page app with server-side rendering
- **Database:** PostgreSQL, started with Docker Compose
- **Media storage:** Cloudinary

---

## Features

- Register and log in with username/email + password, or with **Google** / **GitHub**
- Create, edit and delete posts with tags and multiple images or videos
- Likes and threaded comments
- Follow users, followers / following lists, user search
- Notifications with an unread counter
- Profile pages with avatar and bio
- Report posts or users
- Admin dashboard: review reports, ban or deactivate users, hide or delete posts, remove comments
- Upload validation: only real images (JPEG, PNG, GIF, WebP) and videos (MP4, WebM, MOV) are accepted, checked by file content and not only by file name
- Rate limiting on the API

---

## Technologies

| Part | Stack |
| --- | --- |
| Backend | Java 17, Spring Boot 4.0.5, Spring Web MVC, Spring Data JPA (Hibernate), Spring Security, OAuth2 Client, JJWT 0.11.5, Bean Validation, Bucket4j (rate limiting), Apache Tika (file type detection), Lombok, Maven |
| Frontend | Angular 21, Angular Material + CDK, Angular SSR with Express 5, RxJS 7.8, TypeScript 5.9, Sass, Vitest |
| Database | PostgreSQL 15 |
| Media | Cloudinary SDK 1.36.0 |
| Tooling | Docker Compose, GitHub Actions (CI build on `main`) |

---

## Before you start

Install:

| Tool | Version |
| --- | --- |
| JDK | 17 or newer |
| Node.js | `^20.19`, `^22.12` or `>=24` |
| npm | 10.x |
| Docker + Docker Compose | recent |
| Git | any |

You also need three free accounts (you will create keys in step 2):

- **Cloudinary** (stores images and videos)
- **Google Cloud** (Google login)
- **GitHub** (GitHub login)

---

## Quick start

### 1. Clone the repository

```bash
git clone https://github.com/Marouane-EN/01blog.git
cd 01blog
```

### 2. Get your keys

**Cloudinary**

1. Sign in at https://cloudinary.com
2. Open **Settings > API Keys** and copy the **API environment variable**. It looks like `cloudinary://<api_key>:<api_secret>@<cloud_name>`.

**Google login**

1. Go to https://console.cloud.google.com/apis/credentials and create an **OAuth client ID** of type *Web application*.
2. Add this **Authorized redirect URI**: `http://localhost:8080/login/oauth2/code/google`
3. Copy the client ID and client secret.

**GitHub login**

1. Go to https://github.com/settings/developers and create a **New OAuth App**.
2. Homepage URL: `http://localhost:4200`
3. Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Generate a client secret and copy both values.

### 3. Create your configuration files

The real configuration files contain secrets, so they are git-ignored. The repository ships a template for each. Copy both:

```bash
cd backend/src/main/resources
cp application.example.properties application.properties
cp application-secret.example.properties application-secret.properties
```

On Windows (Command Prompt):

```bat
copy application.example.properties application.properties
copy application-secret.example.properties application-secret.properties
```

Then:

- **`application.properties`**: nothing to change. It only holds settings and `${...}` placeholders.
- **`application-secret.properties`**: open it and replace every `<placeholder>` with your real value. Each entry has a comment explaining where the value comes from. You will need:
  - the database password (the `POSTGRES_PASSWORD` in `backend/compose.yaml`)
  - the Google and GitHub client ID and secret from step 2
  - a JWT secret: run `openssl rand -base64 32` and paste the result (Git Bash on Windows includes `openssl`)
  - the admin username, email and password you want for the first administrator
  - the Cloudinary URL from step 2

> **Never commit `application.properties` or `application-secret.properties`.** If a secret is ever pushed by accident, create a new one (new key or password) instead of only deleting the commit.

### 4. Start the database

From the `backend/` folder:

```bash
cd ../../..        # back to backend/ (skip if you are already there)
docker compose up -d
```

This starts PostgreSQL 15 in a container named `01blog_postgres`:

| Setting | Value |
| --- | --- |
| Host port | `5433` |
| Database | `blog_db` |
| User | `admin` |
| Password | `POSTGRES_PASSWORD` from `compose.yaml` |

The data is kept in a Docker volume, so it survives restarts.

### 5. Run the backend

Still in `backend/`:

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`.

Wait for the log to show that the application started. On the first run it also prints that the **Super Admin was created**. The API now listens on **http://localhost:8080**, and Hibernate creates all tables automatically.

### 6. Run the frontend

Open a second terminal:

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200** in your browser.

### 7. Log in

- **As a normal user:** click *Sign up*, or continue with Google / GitHub.
- **As the administrator:** log in with the `ADMIN_USERNAME` (or `ADMIN_EMAIL`) and `ADMIN_PASSWORD` you wrote in `application-secret.properties`, then open the admin dashboard.

---

## Everyday commands

```bash
# Backend (from backend/)
docker compose up -d                 # start the database
./mvnw spring-boot:run               # run the API
./mvnw test                          # run tests
./mvnw clean package -DskipTests     # build the JAR

# Frontend (from frontend/)
npm start                            # dev server on http://localhost:4200
npm run build                        # production build
npm test                             # unit tests

# Database
docker exec -it 01blog_postgres psql -U admin -d blog_db   # open a SQL shell
docker compose down                  # stop the database, keep the data
docker compose down -v               # stop it and DELETE all data
```

---

## Project structure

```
.
├── backend/
│   ├── compose.yaml                          # PostgreSQL container
│   ├── pom.xml
│   └── src/main/
│       ├── java/_Blog_Backend/
│       │   ├── config/                       # Security, JWT filter, CORS, Cloudinary, admin seeder
│       │   ├── controller/                   # REST endpoints
│       │   ├── service/                      # Business logic
│       │   ├── repository/                   # Spring Data JPA repositories
│       │   ├── entity/                       # JPA entities
│       │   ├── dto/                          # Request / response objects
│       │   └── exception/                    # Global error handling
│       └── resources/
│           ├── application.example.properties          # template (committed)
│           ├── application-secret.example.properties   # template (committed)
│           ├── application.properties                  # your copy (git-ignored)
│           └── application-secret.properties           # your secrets (git-ignored)
└── frontend/
    └── src/app/
        ├── core/                             # Services, guards, interceptors
        ├── features/                         # auth, posts, profile, admin, ...
        └── shared/                           # Reusable components
```

---

## Troubleshooting

| Problem | Likely cause and fix |
| --- | --- |
| `Could not resolve placeholder 'DB_URL'` (or another variable) | `application-secret.properties` is missing, or that variable is empty. Redo step 3. |
| `Connection refused` when the backend starts | The database is not running. Run `docker compose up -d` in `backend/`. |
| `password authentication failed` | `DB_PASSWORD` does not match `POSTGRES_PASSWORD` in `compose.yaml`. If you changed the password after the first start, run `docker compose down -v` once so PostgreSQL starts fresh. |
| Google/GitHub shows `redirect_uri_mismatch` | The callback URL in the provider settings must be exactly `http://localhost:8080/login/oauth2/code/google` (or `.../github`). |
| JWT / `WeakKeyException` error | `JWT_SECRET_KEY` is too short. Generate a new one with `openssl rand -base64 32`. |
| `Unsupported file type` on upload | Only JPEG, PNG, GIF, WebP images and MP4, WebM, MOV videos are accepted. |
| Upload fails with a size error | The file is over the 100 MB limit, or over your Cloudinary plan's limit. |
| `npm install` fails with an engine error | Use a supported Node.js version (see *Before you start*). |
| Port `8080`, `4200` or `5433` already in use | Stop the program using it, then start again. |
