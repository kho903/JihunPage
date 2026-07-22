# Docker Development Environment

This document describes the Docker-based development environment for JihunPage.

Docker Compose runs the following services together:

- React frontend
- Spring Boot backend
- MySQL database

## Architecture

```text
Browser
   │
   ▼
React / Vite
   │
   ▼
Spring Boot
   │
   ▼
MySQL 8.4
   │
   ▼
Docker Volume
```

## Requirements

- Docker
- Docker Compose

## Environment Variables

Create a `.env` file in the project root.

```dotenv
MYSQL_DATABASE=jihunpage
MYSQL_USER=jihun
MYSQL_PASSWORD=your_mysql_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password
```

Use `.env.example` as a template.

The local `.env` file is excluded from Git because it contains database credentials.

## Start the Application

Run the following command from the project root:

```bash
docker compose up --build
```

To run the containers in the background:

```bash
docker compose up -d --build
```

## Service URLs

| Service    | URL                              |
| ---------- | -------------------------------- |
| Frontend   | http://localhost:5173            |
| Backend    | http://localhost:8080            |
| Health API | http://localhost:8080/api/health |
| MySQL      | localhost:3306                   |

## Check Container Status

```bash
docker compose ps
```

The MySQL container should display a healthy status.

```text
mysql      running (healthy)
backend    running
frontend   running
```

## View Logs

View logs for all services:

```bash
docker compose logs -f
```

View backend logs:

```bash
docker compose logs -f backend
```

View MySQL logs:

```bash
docker compose logs -f mysql
```

## MySQL Connection

The Spring Boot backend connects to MySQL through the Docker Compose service name:

```text
jdbc:mysql://mysql:3306/jihunpage
```

Inside the Docker network, `mysql` is the hostname of the MySQL container.

A local database client such as MySQL Workbench can connect using:

```text
Host: localhost
Port: 3306
Database: jihunpage
User: value of MYSQL_USER
Password: value of MYSQL_PASSWORD
```

## Check MySQL Version

```bash
docker compose exec mysql sh -c \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT VERSION();"'
```

Expected version:

```text
8.4.10
```

## Check Database Tables

```bash
docker compose exec mysql sh -c \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SHOW TABLES;"'
```

JPA creates the required tables when the backend starts.

## Data Persistence

MySQL data is stored in the `mysql_data` Docker volume.

The data remains after running:

```bash
docker compose restart backend
```

It also remains after stopping and recreating the containers:

```bash
docker compose down
docker compose up -d
```

The following command removes named volumes, including all MySQL data:

```bash
docker compose down -v
```

Do not use `-v` when the database data must be preserved.

## Development Workflow

Frontend changes are automatically reflected through Vite HMR.

After changing backend source code, restart the backend service:

```bash
docker compose restart backend
```

## Stop the Application

```bash
docker compose down
```

## Rebuild the Services

Rebuild all services:

```bash
docker compose build
```

Rebuild only the backend:

```bash
docker compose build backend
```

Rebuild without using the Docker build cache:

```bash
docker compose build --no-cache
```

## Validate the Compose Configuration

```bash
docker compose config
```

This command validates the Compose file and resolves environment variables.

The resolved output may contain database credentials, so it should not be shared publicly.
