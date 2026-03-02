# busnau-api

Task manager demo REST API built with Spring Boot 4, Spring Data JPA, Spring Security, Flyway, and PostgreSQL.

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
DB_PORT=5433
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
spring.datasource.url=jdbc:postgresql://localhost:5433/your_db_name
spring.datasource.username=your_db_user
spring.datasource.password=supersecret
jwt.secret=your-very-secure-jwt-secret-key-at-least-256-bits-long
jwt.expiration=86400000
jwt.refreshExpiration=604800000
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

## Useful DB commands

```sh
# Open a psql shell inside the running container
docker compose exec db psql -U your_db_user -d your_db_name

# List all databases
docker compose exec db psql -U your_db_user -l

# Tail Postgres logs
docker compose logs -f db
```

---

## Common Gradle tasks

```sh
./gradlew bootRun       # run the app
./gradlew test          # run tests
./gradlew build         # compile + test + package
./gradlew bootJar       # build executable JAR -> build/libs/
./gradlew spotlessApply # auto-format code
./gradlew spotlessCheck # check code formatting (fails if not formatted)
```

---

## API Testing with Postman

The project includes tools to generate and transform a Postman collection from the OpenAPI specification for easy API testing.

### Generate Postman Collection

Ensure the app is running on `localhost:8080`, then run the provided script:

```sh
./postman/generate_postman_collection.sh
```

This script:

1. Fetches the OpenAPI JSON from `/v3/api-docs`
2. Converts it to a Postman collection using `openapi-to-postmanv2`
3. Transforms the collection to use environment variables and automatic token handling
4. Outputs `myapp-collection_transformed.json`

### Import and Configure in Postman

1. Import `myapp-collection_transformed.json` into Postman.
2. Create a new environment with the following variables:
   - `base_url`: `http://localhost:8080`
   - `username`: Your test username (e.g., `user`)
   - `password`: Your test password
   - `accessToken`: (leave empty, set automatically)
   - `refreshToken`: (leave empty, set automatically)

3. Run the "Login" request first — it will automatically set `accessToken` and `refreshToken` in the environment.
4. All other requests will use `{{accessToken}}` for Bearer authentication.

### Manual Generation (Alternative)

If you prefer manual steps:

```sh
# Fetch OpenAPI spec
curl -s http://localhost:8080/v3/api-docs > openapi.json

# Convert to Postman collection
npx openapi-to-postmanv2 -s openapi.json -o myapp-collection.json -p -O folderStrategy=Tags

# Transform for env vars and auth
python3 postman/transform_to_postman.py myapp-collection.json

# Clean up
rm openapi.json
```

The generated collection files are ignored by Git (see `.gitignore`).

---

## Project structure

```
src/
  main/
    java/com/bedoyarama/busnau/   # application source
    resources/
      application.properties           # base config (env-var driven, committed)
      application-local.properties     # local dev overrides (git-ignored)
      db/migration/                    # Flyway migration scripts
.env.example                           # env var template (committed)
.env                                   # real credentials (git-ignored)
docker-compose.yml                     # local Postgres service
postman/                               # Postman collection generation tools
```
