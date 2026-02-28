# busnau-api

REST API built with Spring Boot 4, Spring Data JPA, Spring Security, and PostgreSQL.

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Java | 21 |
| Docker & Docker Compose | Docker Desktop 4+ |
| (optional) Gradle | 8+ — or use the included `./gradlew` wrapper |

---

## Local setup

### 1. Clone the repo

```sh
git clone <repo-url>
cd busnau-api
```

### 2. Configure environment variables

```sh
cp .env.example .env
```

Open `.env` and fill in your values:

```dotenv
DB_HOST=localhost
DB_PORT=5432
DB_NAME=your_db_name
DB_USER=your_db_user
DB_PASSWORD=supersecret
```

> **Why is `.env` needed?** Docker Compose reads it to provision the Postgres container — it creates the database and user on first start.
> `.env` is git-ignored and never committed. Keep real credentials out of source control.

### 3. Configure the Spring local profile

```sh
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Open `application-local.properties` and fill in the same credentials you used in `.env`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_db_user
spring.datasource.password=supersecret
```

> **Why is this needed separately?** Spring Boot reads this file directly at runtime — it does not read `.env`.
> The two files must have matching DB credentials: `.env` tells Docker what to create, `application-local.properties` tells Spring how to connect to it.
> This file is also git-ignored.

---

## Running locally

### Start the database

```sh
docker compose up -d
```

This starts a PostgreSQL 17 container using the credentials from `.env`.

Verify it is healthy:

```sh
docker compose ps
```

### Start the application

**Option A — using the `local` Spring profile (recommended, no extra env vars needed):**

```sh
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

**Option B — using environment variables directly:**

```sh
export $(cat .env | xargs)
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

---

## Stopping

```sh
# Stop the database container (data is preserved in a Docker volume)
docker compose stop

# Stop and remove the container + volume (wipes all data)
docker compose down -v
```

---

## Common Gradle tasks

```sh
./gradlew bootRun       # run the app
./gradlew test          # run tests
./gradlew build         # compile + test + package
./gradlew bootJar       # build executable JAR -> build/libs/
```

---

## Project structure

```
src/
  main/
    java/com/example/busnau/   # application source
    resources/
      application.properties           # base config (env-var driven, committed)
      application-local.properties     # local dev overrides (git-ignored)
docker-compose.yml                     # local Postgres service
.env.example                           # env var template (committed)
.env                                   # real credentials (git-ignored)
```

