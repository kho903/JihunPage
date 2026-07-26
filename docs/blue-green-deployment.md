# Manual Blue-Green Deployment

## Overview

The development environment provides two Spring Boot backend environments:

- `backend-blue`
- `backend-green`

Only one environment receives user traffic through Nginx at a time

The inactive environment can be started and verified before switching traffic, allowing development without immediately stopping the currently active environment

## Architecture

### Blue Active

```text
                         ┌── backend-blue ── MySQL
Browser → Nginx ─────────┤                  Redis Session
                         │
                         └── backend-green
                              └── Standby
```

### Green Active

```text
                         ┌── backend-blue
Browser → Nginx ─────────┤    └── Standby
                         │
                         └── backend-green ── MySQL
                                             Redis Session
```

Both environments use:

- The same MySQL database
- The same Redis session store
- The same gallery upload directory
- The same Spring Boot application code

## Backend Ports

| Environment | Host Port | Container Port |
| :---------- | :-------: | :------------: |
| Blue        |  `8081`   |     `8080`     |
| Green       |  `8082`   |     `8080`     |

Direct health checks:

```bash
curl -si http://localhost:8081/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'

curl -si http://localhost:8082/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

Expected headers:

```text
X-Backend-Instance: backend-blue
X-Backend-Instance: backend-green
```

## Upstrean Configuration

The available upstream configuration files are:

```text
nginx/upstreams/
├── backend-blue.conf
├── backend-green.conf
└── active-backend.conf
```

Blue configuration:

```nginx
upstream backend_upstream {
    server backend-blue:8080;
}
```

Green Configuration:

```nginx
upstream backend_upstream {
    server backend-green:8080;
}
```

Nginx reads only the active configuration:

```text
nginx/upstreams/active-backend.conf
```

## Check the Active Environment

```bash
cat nginx/upstreams/active-backend.conf
```

Check through Nginx:

```bash
curl -si http://localhost/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

## Switch from Blue to Green

### 1. Check Green health

Traffic must not be switched before Green passes its health check.

```bash
curl -fsS http://localhost:8082/api/health
```

Check the instance header:

```bash
curl -si http://localhost:8082/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-green
```

### 2. Activate Green

```bash
cp \
  nginx/upstreams/backend-green.conf \
  nginx/upstreams/active-backend.conf
```

### 3. Validate the Nginx configuration

```bash
docker compose exec nginx nginx -t
```

Do not reload Nginx when the configuration test fails.

### 4. Reload Nginx

```bash
docker compose exec nginx -s reload
```

### 5. Verify the switch

```bash
for i in {1..5}; do
  curl -si http://localhost/api/health \
    | grep -Ei 'HTTP/|x-backend-instance:'
```

All responses should contain:

```text
X-Backend-Instance: backend-green
```

## Roll Back from Green to Blue

### 1. Activate Blue

```bash
cp \
  nginx/upstreams/backend-blue.conf \
  nginx/upstreams/active-backend.conf
```

### 2. Validate the configuration

```bash
docker compose exec nginx nginx -t
```

### 3. Reload Nginx

```bash
docker compose exec nginx nginx -s reload
```

### 4. Verify the rollback

```bash
for i in {1..5}; do
  curl -si http://localhost/api/health \
    | grep -Ei 'HTTP/|x-backend-instance:'
done
```

All responses should contain:

```text
X-Backend-Instance: backend-blue
```

## Sharded-Session Verification

Redis allows the authenticated HTTP session remain valid after switching environments

### 1. Log in while Blue is active

```bash
COOKIE_FILE=/tmp/jihunpage-blue-green-session.txt

rm -f "$COOKIE_FILE"
```

Replace the account information with an existing user.

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

### 2. Verify authentication

```bash
curl -si \
  -b "$COOKIE_FILE" \
  http://localhost/api/auth/me \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

### 3. Switch to Green

```bash
cp \
  nginx/upstreams/backend-green.conf \
  nginx/upstreams/active-backend.conf

docker compose exec nginx nginx -t
docker compose exec nginx nginx -s reload
```

### 4. Reuse the existing cookie

```bash
curl -si \
  -b "$COOKIE_FILE" \
  http://localhost/api/auth/me \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

Expected result:

```text
HTTP/1.1 200
X-Backend-Instance: backend-green
```

The user remains authenticated because both environments read the same session from Redis

```text
Blue creates the session
-> Session is stored in Redis
-> Nginx switches to Green
-> Green reads the same Redis session
-> Authentication remains valid
```

The same session should remain valid after rolling back to Blue

## Manual Deployment Flow

1. Keep the current environment active
2. Start the inactive environment
3. Check the inactive environment directly
4. Confirm its health and instance name
5. Replace active-backend.conf
6. Validate the Nginx configuration
7. Reload Nginx
8. Verify the active environment
9. Verify the existing login session
10. Keep the previous environment available for rollback

## Import Limitations

This is a development-oriented manual Blue-Green structure

It does not yet include:

- Production Docker images
- Automatic health-check retries
- Deployment scripts
- Automatic rollback
- Github Actions
- AWS deployment
- Database migration management
- Versione Docker images
- Zero-downtime frontend deployment

The current active file is also tracked in Git. A later deployment script will manage environment switching more safely.
