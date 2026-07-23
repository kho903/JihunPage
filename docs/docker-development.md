# Docker Development Environment

This document describes the Docker-based development environment for JihunPage.

Docker Compose runs the following services together:

- Nginx reverse proxy
- React frontend
- Spring Boot backend
- MySQL database
- Redis session store

## Documentation

- [Docker Development Environment](./docs/docker-development.md)
- [Nginx Load Balancing](./docs/nginx-load-balancing.md)

## Architecture

```text
Browser
   │
   │ http://localhost
   │ SESSION Cookie
   ▼
Nginx :80
   ├── /          → frontend:5173
   ├── /api       → backend:8080
   └── /uploads   → backend:8080

backend:8080
   ├── mysql:3306
   │      │
   │      ▼
   │  mysql_data
   │
   └── redis:6379
          │
          ▼
      redis_data
```

Nginx is the single entry point for the Docker development environment.

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

| Service     | URL                         | Purpose                              |
| ----------- | --------------------------- | ------------------------------------ |
| Application | http://localhost            | Main entry point through Nginx       |
| Frontend    | http://localhost:5173       | Direct frontend access for debugging |
| Backend     | http://localhost:8080       | Direct backend access for debugging  |
| Health API  | http://localhost/api/health | Backend health check through Nginx   |
| MySQL       | localhost:3306              | Application database                 |
| Redis       | localhost:6379              | HTTP session storage                 |

## Check Container Status

```bash
docker compose ps
```

The MySQL and Redis containers should display a healthy status.

```text
mysql      running (healthy)
redis      running (healthy)
backend    running
frontend   running
nginx      running
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

View Nginx logs:

```bash
docker compose logs -f nginx
```

## Nginx Reverse Proxy

Nginx is the single entry point for the Docker development environment.

The Nginx configuration file is located at:

```text
nginx/default.dev.conf
```

Docker Compose mounts this file into the container as:

```text
/etc/nginx/conf.d/default.conf
```

The configuration file is mounted as read-only.

### Proxy Routes

Nginx routes requests according to their URL paths:

| Request path | Target service  | Purpose                                       |
| ------------ | --------------- | --------------------------------------------- |
| `/`          | `frontend:5173` | React application and Vite development assets |
| `/api`       | `backend:8080`  | Spring Boot REST APIs                         |
| `/uploads`   | `backend:8080`  | Uploaded gallery images                       |

For example:

```text
Browser request:
http://localhost/api/auth/me

Internal request:
http://backend:8080/api/auth/me
```

The browser only needs to access `http://localhost`.

Docker Compose service names such as `frontend` and `backend` work as hostnames inside the Docker network.

### Vite Hot Module Replacement

Vite uses a WebSocket connection for Hot Module Replacement.

Nginx forwards the WebSocket upgrade headers so that frontend source changes are reflected without manually refreshing the browser.

A successful WebSocket connection can be confirmed in the browser Network panel:

```text
Status Code: 101 Switching Protocols
```

### React Router

Frontend routes are proxied to the Vite development server.

Therefore, routes such as the following continue to work after browser refresh:

```text
/login
/signup
/gallery
/members/{userid}/gallery
```

This behavior applies to the current Vite development environment.

A production environment that serves React build files directly from Nginx will require an SPA fallback configuration.

### Upload Size

Nginx allows requests up to 6 MB:

```nginx
client_max_body_size 6m;
```

This matches the Spring Boot multipart request limit.

Requests exceeding the limit may return:

```text
413 Request Entity Too Large
```

### Nginx Commands

Test the configuration:

```bash
docker compose exec nginx nginx -t
```

View the configuration mounted inside the container:

```bash
docker compose exec nginx \
  cat /etc/nginx/conf.d/default.conf
```

Restart Nginx:

```bash
docker compose restart nginx
```

View the latest Nginx logs:

```bash
docker compose logs --no-color --tail=100 nginx
```

## Nginx Load Balancing

The development environment runs multiple Spring Boot backend instances behind Nginx.

For load-balancing architecture, Redis shared-session verification, and backend failure testing, see:

- [Nginx Load Balancing](./nginx-load-balancing.md)

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

## Troubleshooting

### 502 Bad Gateway

A `502 Bad Gateway` response means that Nginx is running, but it cannot connect to the frontend or backend service.

Check the container status:

```bash
docker compose ps
```

Check the service logs:

```bash
docker compose logs --no-color --tail=100 nginx
docker compose logs --no-color --tail=100 frontend
docker compose logs --no-color --tail=100 backend
```

### Port 80 Is Already in Use

If the Nginx container cannot start because port `80` is already allocated, check the process using the port on macOS:

```bash
sudo lsof -nP -iTCP:80 -sTCP:LISTEN
```

### Nginx Configuration Error

Test the configuration:

```bash
docker compose exec nginx nginx -t
```

Then inspect the logs:

```bash
docker compose logs --no-color --tail=100 nginx
```
