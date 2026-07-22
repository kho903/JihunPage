# Docker Development Environment

This document describes the Docker-based development environment for JihunPage.

Docker Compose runs the following services together:

- React frontend
- Spring Boot backend
- MySQL database
- Redis session store

## Architecture

```text
Browser
   │
   │ SESSION Cookie
   ▼
React / Vite
   │
   ▼
Spring Boot
   ├──────────────► MySQL 8.4
   │                  │
   │                  ▼
   │              mysql_data
   │
   └──────────────► Redis 7.4
                      │
                      ▼
                  redis_data
```

MySQL stores persistent application data such as members and gallery records.

Redis stores HTTP session data managed by Spring Session.

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
| Redis      | localhost:6379                   |

## Check Container Status

```bash
docker compose ps
```

The MySQL container should display a healthy status.

```text
mysql      running (healthy)
redis      running (healthy)
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

View Redis logs:

```bash
docker compose logs -f redis
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

## Redis Session Storage

The backend connects to Redis through the Docker Compose service name:

```text
redis:6379
```

Spring Session stores HTTP sessions using the following namespace:

```text
jihunpage:session
```

After logging in, inspect the Redis session keys:

```bash
docker compose exec redis redis-cli \
  --scan \
  --pattern 'jihunpage:session:*'
```

A session key looks similar to:

```text
jihunpage:session:sessions:<session-id>
```

Check the remaining session lifetime:

```bash
docker compose exec redis redis-cli TTL \
  "jihunpage:session:sessions:<session-id>"
```

A positive result represents the remaining session lifetime in seconds.

```text
1800
1799
1798
```

Inspect the fields stored in the session:

```bash
docker compose exec redis redis-cli HKEYS \
  "jihunpage:session:sessions:<session-id>"
```

The session contains metadata and application session attributes such as:

```text
creationTime
lastAccessedTime
maxInactiveInterval
sessionAttr:LOGIN_MEMBER_ID
```

The Spring Session cookie name is:

```text
SESSION
```

The session remains valid after restarting only the backend container:

```bash
docker compose restart backend
```

After the backend starts again, refresh the browser and verify that the authenticated state remains available.

## Data Persistence

MySQL application data is stored in the `mysql_data` Docker named volume.

Redis session data is stored in the `redis_data` Docker named volume.

Both remain after restarting the backend:

```bash
docker compose restart backend
```

They also remain after stopping and recreating the containers:

```bash
docker compose down
docker compose up -d
```

The following command removes both named volumes:

```bash
docker compose down -v
```

This deletes:

- MySQL application data
- Redis session data

Uploaded gallery images are stored in the local `backend/uploads` directory.

Because this directory is bind-mounted from the host, uploaded files are not deleted when Docker named volumes are removed.
