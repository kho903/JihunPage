# Nginx Load Balancing

## Overview

The development environment runs two Spring Boot backend instances behind Nginx.

Nginx distributes API requests between the backend instances, while both instances share the same MySQL database and Redis-backed HTTP sessions.

The purpose of this configuration is to verify:

- Nginx round-robin load balancing
- Authentication across multiple backend instances
- Redis-backed shared HTTP sessions
- Service continuity when one backend instance stops
- The basic architecture required for future Blue-Green deployment

The main application entry point is:

```text
http://localhost
```

## Architecture

```text
                         ┌── backend-1:8080 ──┐
Browser                  │                    │
   │                     │                    ├── MySQL
   ▼                     │                    │
Nginx :80 ───────────────┤                    └── Redis Session
                         │
                         └── backend-2:8080
```

Request routing:

```text
/          → frontend:5173
/api/      → backend-1:8080 or backend-2:8080
/uploads/  → backend-1:8080 or backend-2:8080
```

Both backend instances:

- Run the same Spring Boot application
- Use the same MySQL database
- Use the same Redis session store
- Share the gallery upload directory
- Return their instance name through an HTTP response header

## Backend Instances

Two backend services run in Docker Compose.

| Service     | Container Port | Host Port |
| :---------- | :------------: | :-------: |
| `backend-1` |     `8080`     |  `8081`   |
| `backend-2` |     `8080`     |  `8082`   |

The host ports are used only for direct development and debugging.

Check `backend-1` directly:

```bash
curl -si http://localhost:8081/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-1
```

Check `backend-2` directly:

```bash
curl -si http://localhost:8082/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-2
```

Applications should normally use the Nginx entry point rather than accessing these ports directly.

```text
http://localhost
```

## Backend Instance Identification

Each backend container receives its instance name through an environment variable.

```yaml
backend-1:
  environment:
    APP_INSTANCE_NAME: backend-1

backend-2:
  environment:
    APP_INSTANCE_NAME: backend-2
```

Spring Boot maps these environment variables to the following property:

```text
app.instance-name
```

The backend adds the instance name to every HTTP response.

```http
X-Backend-Instance: backend-1
```

```http
X-Backend-Instance: backend-2
```

This header makes it possible to identify which backend instance handled a request.

The header is intended for development verification and should not expose internal infrastructure information in production.

## Nginx Upstream Configuration

Nginx defines an upstream group containing both backend services.

```nginx
upstream backend_upstream {
    server backend-1:8080;
    server backend-2:8080;
}
```

API requests are proxied to the upstream group.

```nginx
location /api/ {
    proxy_pass http://backend_upstream;

    proxy_http_version 1.1;

    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Uploaded gallery files are also served through the upstream group.

```nginx
location /uploads/ {
    proxy_pass http://backend_upstream;

    proxy_http_version 1.1;

    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Nginx uses round-robin load balancing when no other balancing algorithm is configured.

Sticky sessions are not required because both backend instances use the same Redis session store.

Check the Nginx configuration:

```bash
docker compose exec nginx nginx -t
```

Reload the configuration:

```bash
docker compose exec nginx nginx -s reload
```

Restart Nginx when necessary:

```bash
docker compose restart nginx
```

## Load-Balancing Verification

Send multiple health-check requests through Nginx.

```bash
for i in {1..10}; do
  curl -si http://localhost/api/health \
    | grep -i x-backend-instance
done
```

Both backend instance names should appear.

```text
X-Backend-Instance: backend-1
X-Backend-Instance: backend-2
X-Backend-Instance: backend-1
X-Backend-Instance: backend-2
```

The exact response order is not important.

Successful verification requires:

```text
backend-1 handles requests
backend-2 handles requests
All health-check requests return HTTP 200
```

## Redis Shared-Session Verification

Both backend instances store HTTP sessions in the same Redis server.

This allows a user to log in through one backend instance and continue using the same session through another instance.

```text
Login handled by backend-1
→ Session stored in Redis
→ Next request handled by backend-2
→ backend-2 reads the session from Redis
→ Authentication remains valid
```

### Create a cookie file

```bash
COOKIE_FILE=/tmp/jihunpage-session.txt

rm -f "$COOKIE_FILE"
```

### Log in through Nginx

Replace the login values with an existing account.

```bash
curl -si \
  -c "$COOKIE_FILE" \
  -H "Content-Type: application/json" \
  -d '{
    "userid": "YOUR_USER_ID",
    "userpwd": "YOUR_PASSWORD"
  }' \
  http://localhost/api/auth/login \
  | grep -Ei 'HTTP/|set-cookie:|x-backend-instance:'
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-1
Set-Cookie: SESSION=...
```

The login request may also be handled by `backend-2`.

### Check the saved session cookie

```bash
cat "$COOKIE_FILE"
```

The cookie file should contain a `SESSION` cookie.

```text
localhost    FALSE    /    FALSE    0    SESSION    ...
```

### Send authenticated requests repeatedly

```bash
for i in {1..10}; do
  echo "Request $i"

  curl -si \
    -b "$COOKIE_FILE" \
    http://localhost/api/auth/me \
    | grep -Ei 'HTTP/|x-backend-instance:'

  echo
done
```

Successful verification requires:

```text
Every request returns HTTP 200
backend-1 and backend-2 both handle requests
Authentication remains valid on both instances
```

### Check the authenticated member response

```bash
curl -s \
  -b "$COOKIE_FILE" \
  http://localhost/api/auth/me
```

The authenticated member information should be returned.

```json
{
  "id": 1,
  "userid": "YOUR_USER_ID",
  "username": "YOUR_NAME"
}
```

### Check the Redis session

```bash
docker compose exec redis redis-cli \
  --scan \
  --pattern 'jihunpage:session:sessions:*'
```

Expected key format:

```text
jihunpage:session:sessions:<session-id>
```

Check the session TTL:

```bash
SESSION_KEY=$(
  docker compose exec -T redis redis-cli \
    --scan \
    --pattern 'jihunpage:session:sessions:*' \
  | head -n 1
)

docker compose exec redis redis-cli TTL "$SESSION_KEY"
```

A positive value means that the session has an expiration time.

## Backend Failure Verification

The application should continue processing requests when one backend instance stops.

The existing Redis session cookie is reused during this verification.

```bash
COOKIE_FILE=/tmp/jihunpage-session.txt
```

### Stop `backend-1`

```bash
docker compose stop backend-1
```

Check the container state:

```bash
docker compose ps backend-1 backend-2
```

Send authenticated requests:

```bash
for i in {1..5}; do
  curl -si \
    -b "$COOKIE_FILE" \
    http://localhost/api/auth/me \
    | grep -Ei 'HTTP/|x-backend-instance:'

  echo
done
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-2
```

All requests should be handled by `backend-2`, and the login session should remain valid.

Restart `backend-1`:

```bash
docker compose start backend-1
```

Check its logs:

```bash
docker compose logs --tail=50 backend-1
```

Verify it directly:

```bash
curl -si http://localhost:8081/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

### Stop `backend-2`

```bash
docker compose stop backend-2
```

Send authenticated requests again:

```bash
for i in {1..5}; do
  curl -si \
    -b "$COOKIE_FILE" \
    http://localhost/api/auth/me \
    | grep -Ei 'HTTP/|x-backend-instance:'

  echo
done
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-1
```

All requests should be handled by `backend-1`, and the login session should remain valid.

Restart `backend-2`:

```bash
docker compose start backend-2
```

Check the final container state:

```bash
docker compose ps backend-1 backend-2 nginx mysql redis
```

Verify that both backend instances receive requests again:

```bash
for i in {1..6}; do
  curl -si http://localhost/api/health \
    | grep -i x-backend-instance
done
```

The complete failure-verification flow is:

```text
One backend instance stops
→ Nginx forwards requests to the available instance
→ The available instance reads the existing session from Redis
→ Authentication remains valid
→ Requests continue successfully
```

## Shared Gallery Upload Directory

Both backend instances mount the same gallery upload directory.

```yaml
backend-1:
  volumes:
    - ./backend/uploads:/app/uploads

backend-2:
  volumes:
    - ./backend/uploads:/app/uploads
```

This allows either backend instance to read uploaded gallery images.

```text
Image uploaded through backend-1
→ File saved in the shared directory
→ Image request handled by backend-2
→ The same file is returned
```

This configuration is suitable for the current single-host development environment.

A distributed production environment should use external object storage such as Amazon S3 instead of a local bind mount.

## Troubleshooting

### Nginx returns `502 Bad Gateway`

Check the backend containers:

```bash
docker compose ps backend-1 backend-2
```

Check the backend logs:

```bash
docker compose logs --tail=100 backend-1 backend-2
```

Check both backend instances directly:

```bash
curl -i http://localhost:8081/api/health
curl -i http://localhost:8082/api/health
```

Check the Nginx configuration:

```bash
docker compose exec nginx nginx -t
```

### Nginx uses an old backend container address

A backend container can receive a new Docker network IP when it is recreated.

Nginx may temporarily continue using an address resolved before the backend recreation.

A typical error log is:

```text
connect() failed (111: Connection refused) while connecting to upstream
```

Restart Nginx so that the backend service names are resolved again.

```bash
docker compose restart nginx
```

Verify the connection:

```bash
curl -i http://localhost/api/health
```

### Port `8081` or `8082` is already in use

Check which process uses the port:

```bash
lsof -nP -iTCP:8081 -sTCP:LISTEN
lsof -nP -iTCP:8082 -sTCP:LISTEN
```

Check Docker containers exposing the ports:

```bash
docker ps \
  --format "table {{.Names}}\t{{.Ports}}"
```

Stop an unnecessary process or container before starting the backend instances again.

### Load balancing works but authentication fails

Check that both backend instances use the same Redis server.

```yaml
SPRING_DATA_REDIS_HOST: redis
SPRING_DATA_REDIS_PORT: 6379
```

Check the Redis session keys:

```bash
docker compose exec redis redis-cli \
  --scan \
  --pattern 'jihunpage:session:sessions:*'
```

Check that the request includes the saved `SESSION` cookie:

```bash
curl -si \
  -b "$COOKIE_FILE" \
  http://localhost/api/auth/me
```
