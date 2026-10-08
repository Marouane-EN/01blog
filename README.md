# 01Blog

A full-stack social blogging platform where users publish posts with images and videos, follow other users, like and comment, and report content. Administrators moderate the platform from a dedicated dashboard.

- **Backend:** Spring Boot REST API secured with JWT, plus Google and GitHub OAuth2 login
- **Frontend:** Angular single-page app with server-side rendering (SSR)
- **Database:** PostgreSQL (run through Docker Compose)
- **Media storage:** Cloudinary

---

## Features

- Local registration/login (username or email + password) and OAuth2 login with **Google** and **GitHub**
- Create, edit and delete posts with tags and multiple images/videos
- Likes on posts and comments, threaded comments (replies)
- Follow / unfollow users, followers and following lists, user search
- Notifications with an unread counter
- Profile pages with avatar upload and bio
- Report posts or users
- Admin dashboard: review reports, ban/unban users, deactivate accounts, hide or permanently delete posts, remove comments
- Cursor-based pagination for feeds, comments, followers and notifications
- Upload validation: only real images (JPEG, PNG, GIF, WebP) and videos (MP4, WebM, MOV) are accepted, checked by file content and not only by file name
- Rate limiting on the API

---

## Technologies

### Backend

| Technology | Purpose |
| --- | --- |
| Java 17 | Language |
| Spring Boot 4.0.5 | Application framework |
| Spring Web MVC | REST API |
| Spring Data JPA + Hibernate | ORM and database access |
| Spring Security | Authentication and authorization |
| Spring Security OAuth2 Client | Google / GitHub login |
| JJWT 0.11.5 | JSON Web Token creation and validation |
| Bean Validation | Request validation |
| Bucket4j 8.3.0 | Rate limiting |
| Cloudinary SDK 1.36.0 | Image and video storage |
| Apache Tika | Detects the real file type from file content |
| PostgreSQL 15 | Relational database |
| Lombok | Boilerplate reduction |
| Maven (wrapper included) | Build tool |
| GitHub Actions | CI (build on push / pull request to `main`) |

### Frontend

| Technology | Purpose |
| --- | --- |
| Angular 21 | SPA framework (standalone components, signals) |
| Angular Material + CDK | UI components |
| Angular SSR (`@angular/ssr`) with Express 5 | Server-side rendering |
| RxJS 7.8 | Reactive programming / HTTP streams |
| TypeScript 5.9 | Language |
| Sass (SCSS) | Styling |
| Prettier | Code formatting |

### Infrastructure

- **Docker / Docker Compose** for the PostgreSQL container
- **Cloudinary** for media hosting

---

## Prerequisites

Install these before starting:

| Tool | Version |
| --- | --- |
| JDK | 17 or newer |
| Node.js | `^20.19`, `^22.12` or `>=24` |
| npm | 10.x (the project pins `npm@10.8.2`) |
| Docker + Docker Compose | recent |
| Git | any |

You also need free accounts / credentials for:

1. **Cloudinary**: https://cloudinary.com (for the `cloudinary://...` URL)
2. **Google OAuth client**: https://console.cloud.google.com/apis/credentials
3. **GitHub OAuth app**: https://github.com/settings/developers

---

## Setup

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd <repository-folder>
```

### 2. Start the database

From the `backend/` folder:

```bash
cd backend
docker compose up -d
```

This starts a PostgreSQL 15 container named `01blog_postgres` with the settings defined in `backend/compose.yaml`:

| Setting | Value |
| --- | --- |
| Host port | `5433` (mapped to the container's `5432`) |
| Database | `blog_db` |
| User | `admin` |
| Password | see `POSTGRES_PASSWORD` in `compose.yaml` |

Data is stored in the `postgres_data` Docker volume, so it survives container restarts.

### 3. Configure the OAuth2 providers

**Google**

1. Create an OAuth 2.0 Client ID (type: *Web application*).
2. Add this **Authorized redirect URI**: `http://localhost:8080/login/oauth2/code/google`

**GitHub**

1. Create a new OAuth App.
2. Set **Homepage URL** to `http://localhost:4200`
3. Set **Authorization callback URL** to `http://localhost:8080/login/oauth2/code/github`

Keep the client ID and client secret of each for the next step.

### 4. Create the backend configuration

The backend reads its settings from two files in `backend/src/main/resources/`:

| File | Committed? | Contains |
| --- | --- | --- |
| `application.properties` | yes (it holds no secrets) | Spring settings, with placeholders such as `${DB_URL}` |
| `application-secret.properties` | **no** (git-ignored) | The real values for every placeholder |

`application.properties` loads the secret file with `spring.profiles.include=secret`. Create your own secret file from the provided example:

```bash
cd backend/src/main/resources
cp application-secret.properties.example application-secret.properties
```

Then open `application-secret.properties` and fill in every value:

| Variable | What to put |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5433/blog_db` |
| `DB_USERNAME` / `DB_PASSWORD` | Same as `POSTGRES_USER` / `POSTGRES_PASSWORD` in `compose.yaml` |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | From the Google OAuth client (step 3) |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | From the GitHub OAuth app (step 3) |
| `JWT_SECRET_KEY` | Base64 string of at least 32 bytes: `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Token lifetime in milliseconds (`86400000` = 1 day) |
| `ADMIN_USERNAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD` | The administrator account created at first start |
| `CLOUDINARY_URL` | `cloudinary://<api_key>:<api_secret>@<cloud_name>` |

> **Never commit `application-secret.properties`.** If a secret is ever pushed by accident, rotate it (generate a new key or password) instead of only deleting the commit.

The same variable names also work as **environment variables**, which is handy for Docker, CI or production.

### 5. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`.

The API is now available at **http://localhost:8080**. Hibernate creates the tables on first start (`ddl-auto=update`).

### 6. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200**.

The frontend expects the API at `http://localhost:8080/api` (set in `frontend/src/environments/environment.ts`). After a Google/GitHub login, the backend redirects the browser to `http://localhost:4200/oauth2/redirect` with the JWT.

## Useful commands

### Backend

```bash
./mvnw spring-boot:run            # run the API
./mvnw clean package -DskipTests  # build the JAR (same command CI runs)
./mvnw test                       # run tests
```

### Frontend

```bash
npm start            # dev server on http://localhost:4200
npm run build        # production build
npm test             # unit tests (Vitest)
npm run serve:ssr:frontend   # run the SSR server after a build
```

### Database

```bash
# Open an interactive SQL shell
docker exec -it 01blog_postgres psql -U admin -d blog_db

# Look at the users table
docker exec -it 01blog_postgres psql -U admin -d blog_db -c "SELECT id, username, email, role, is_blocked FROM users;"

# Stop the database (data is kept)
docker compose down

# Stop and DELETE all data
docker compose down -v
```

---

## Project structure

```
.
├── backend/
│   ├── compose.yaml                 # PostgreSQL container
│   ├── pom.xml
│   └── src/main/java/_Blog_Backend/
│       ├── config/                  # Security, JWT filter, CORS, Cloudinary, OAuth2 success handler
│       ├── controller/              # REST endpoints
│       ├── service/                 # Business logic (auth, posts, uploads, admin, ...)
│       ├── repository/              # Spring Data JPA repositories
│       ├── entity/                  # JPA entities (User, Post, Comment, Like, Subscription, ...)
│       ├── dto/                     # Request / response objects
│       └── exception/               # Global error handling
└── frontend/
    ├── package.json
    └── src/app/
        ├── core/                    # Services, guards, interceptors, utilities
        ├── features/                # auth, posts, profile, admin, ...
        └── shared/                  # Reusable components (e.g. media carousel)
```

---

## Security notes

- Passwords are hashed with BCrypt.
- Authentication is stateless: every request carries a `Bearer` JWT.
- Blocked or deactivated users are rejected even if their token is still valid.
- Uploaded files are validated by their real content (magic bytes) before being sent to Cloudinary.
- `/api/admin/**` is restricted to the `ADMIN` role.

---

## Troubleshooting

| Problem | Likely cause / fix |
| --- | --- |
| `Connection refused` on startup | The database container is not running. Run `docker compose up -d` in `backend/`. |
| `password authentication failed` | `DB_PASSWORD` in `application-secret.properties` does not match `POSTGRES_PASSWORD` in `compose.yaml`. If you changed it after the first start, run `docker compose down -v` once so PostgreSQL re-initialises. |
| `Could not resolve placeholder 'DB_URL'` (or another variable) | `application-secret.properties` is missing, or one of its variables is not set. |
| Google/GitHub login shows `redirect_uri_mismatch` | The callback URL in the provider's settings does not match `http://localhost:8080/login/oauth2/code/<provider>`. |
| `Unsupported file type` when uploading | Only JPEG, PNG, GIF, WebP images and MP4, WebM, MOV videos are accepted. |
| Upload fails with a size error | The file is larger than the limit (100 MB here). Cloudinary's free plan has lower per-file limits. |
| `npm install` fails with an engine error | Use a supported Node.js version (see Prerequisites). |
