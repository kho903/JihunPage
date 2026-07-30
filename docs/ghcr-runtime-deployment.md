# GHCR Runtime Deployment

## Overview

The project provides a deployment-oriented Docker Compose configuration that runs prebuilt production images from GitHub Container Registry.

The deployment Compose file is:

```text
compose.deploy.yaml
```

Unlike the local production configuration, this file does not build the backend or frontend from source code.

```text
compose.prod.yaml
-> Build application images locally
-> Run the production environment

compose.deploy.yaml
-> Pull prebuilt images from GHCR
-> Run the deployment environment
```

## Deployment Architecture

```text
Browser
  ↓
Nginx :80
  ├── /         → Frontend
  ├── /api      → Active backend
  └── /uploads  → Active backend

Backend
  ├── backend-blue
  └── backend-green

Shared services
  ├── MySQL
  ├── Redis
  └── Gallery uploads
```

Blue and Green use separate backend containers but share:

- MySQL data
- Redis sessions
- Gallery upload files
- Application configuration

## Container Images

The deployment environment uses images published to GHCR.

### Backend

```text
ghcr.io/kho903/jihunpage-backend:<image-tag>
```

### Frontend

```text
ghcr.io/kho903/jihunpage-frontend:<image-tag>
```

The images support:

```text
linux/amd64
linux/arm64
```

## Immutable Image Tags

Deployment should use immutable commit SHA tags rather than relying only on `latest`.

```text
sha-<full-git-commit-sha>
```

Example:

```text
ghcr.io/kho903/jihunpage-backend:sha-0123456789abcdef0123456789abcdef01234567
```

Using SHA tags makes it possible to identify and redeploy the exact image produced from a specific Git commit.

## Deployment Environment File

Create the local deployment environment file from the template.

```bash
cp .env.deploy.example .env.deploy
```

The `.env.deploy` file is excluded from Git.

It contains:

```dotenv
BACKEND_BLUE_IMAGE_TAG=sha-<full-commit-sha>
BACKEND_GREEN_IMAGE_TAG=sha-<full-commit-sha>
FRONTEND_IMAGE_TAG=sha-<full-commit-sha>

MYSQL_DATABASE=jihunpage
MYSQL_USER=jihun
MYSQL_PASSWORD=<mysql-user-password>
MYSQL_ROOT_PASSWORD=<mysql-root-password>
```

Do not commit actual database passwords or private registry credentials.

## Image Tag Separation

Blue and Green use independent image tag variables.

```text
BACKEND_BLUE_IMAGE_TAG
BACKEND_GREEN_IMAGE_TAG
```

This allows different backend versions to run simultaneously.

```text
backend-blue
→ Previous version

backend-green
→ New version
```

The frontend uses:

```text
FRONTEND_IMAGE_TAG
```

## Validate the Configuration

Validate the Compose file and environment variable interpolation.

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  config --quiet
```

No output indicates that the configuration is valid.

Inspect the final image names:

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  config --images
```

The backend and frontend entries should use GHCR SHA tags.

```text
ghcr.io/kho903/jihunpage-backend:sha-...
ghcr.io/kho903/jihunpage-frontend:sha-...
```

## Pull Deployment Images

Pull every image before starting the environment.

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  pull
```

This command should pull prebuilt images without running application Docker builds.

```text
Expected
→ Pulling
→ Pulled

Not expected
→ Building
→ Dockerfile build steps
```

## Start the Deployment Environment

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  up -d
```

Do not add `--build`.

Check container status:

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  ps
```

Expected services:

```text
mysql
redis
backend-blue
backend-green
frontend
nginx
```

MySQL and the backend containers may remain in a starting state briefly during initialization.

## Verify the Runtime Images

Check the Backend Blue image:

```bash
docker inspect \
  "$(docker compose \
    --env-file .env.deploy \
    -p jihunpage-deploy \
    -f compose.deploy.yaml \
    ps -q backend-blue)" \
  --format '{{ .Config.Image }}'
```

Check the Backend Green image:

```bash
docker inspect \
  "$(docker compose \
    --env-file .env.deploy \
    -p jihunpage-deploy \
    -f compose.deploy.yaml \
    ps -q backend-green)" \
  --format '{{ .Config.Image }}'
```

Check the frontend image:

```bash
docker inspect \
  "$(docker compose \
    --env-file .env.deploy \
    -p jihunpage-deploy \
    -f compose.deploy.yaml \
    ps -q frontend)" \
  --format '{{ .Config.Image }}'
```

Each output should start with:

```text
ghcr.io/kho903/
```

## Application Verification

Open the application:

```text
http://localhost
```

Verify:

- Homepage rendering
- Member registration
- Login and logout
- Session persistence after refresh
- Gallery navigation
- Gallery image upload
- Gallery image display
- Gallery image deletion

Check the public endpoint from the terminal:

```bash
curl -I http://localhost
```

Expected result:

```text
HTTP/1.1 200 OK
```

## Blue-Green Environment Setup

Load the deployment variables into the current shell.

```bash
set -a
source .env.deploy
set +a
```

Configure the deployment scripts:

```bash
export COMPOSE_PROJECT_NAME=jihunpage-deploy
export COMPOSE_FILE=compose.deploy.yaml
export PUBLIC_BASE_URL=http://localhost
```

These variables tell the deployment scripts to operate on the GHCR-based deployment environment instead of the local production environment.

## Backend Health Checks

```bash
./scripts/health-check.sh blue
./scripts/health-check.sh green
```

Both backend environments should pass before traffic is switched.

## Detect the Active Environment

```bash
./scripts/detect-environment.sh active
```

Detect the inactive environment:

```bash
./scripts/detect-environment.sh inactive
```

Example:

```text
Active: blue
Inactive: green
```

## Switch Backend Traffic

Switch to Green:

```bash
./scripts/switch-backend.sh green
```

Switch to Blue:

```bash
./scripts/switch-backend.sh blue
```

The script performs:

```text
Target health check
-> Update Nginx upstream
-> Validate Nginx configuration
-> Reload Nginx
-> Verify the public endpoint
```

## Verify the Active Backend

```bash
curl -sS \
  -D - \
  -o /dev/null \
  http://localhost/api/auth/me \
  | grep -i 'X-Backend-Instance'
```

Expected results:

```text
X-Backend-Instance: backend-blue
```

or:

```text
X-Backend-Instance: backend-green
```

An unauthenticated API response may return `401`, but the backend instance header should still identify the active environment.

## Shared Redis Session Verification

To verify shared sessions:

```text
1. Log in while Blue is active
2. Confirm the authenticated state
3. Switch traffic to Green
4. Refresh the browser
5. Confirm that the login session remains active
```

Expected flow:

```text
Browser SESSION cookie
-> Nginx
-> New backend environment
-> Shared Redis session
-> Authenticated state preserved
```

## Shared Gallery Upload Verification

Blue and Green mount the same gallery upload directory.

```text
./backend/uploads
-> /app/uploads
```

To verify shared files:

```text
1. Upload an image through the active backend
2. Switch backend traffic
3. Refresh the gallery
4. Confirm that the image remains available
```

## View Logs

Backend Blue:

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  logs --tail=150 backend-blue
```

Backend Green:

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  logs --tail=150 backend-green
```

Shared services and Nginx:

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  logs --tail=150 mysql redis nginx
```

## Stop the Deployment Environment

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  down
```

Do not add `--volumes` when the MySQL and Redis data should be preserved.

To intentionally delete deployment volumes:

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  down --volumes
```

This permanently removes the deployment database and Redis volume data.

## Local Production vs Deployment

### Local Production

```bash
docker compose \
  -p jihunpage-prod \
  -f compose.prod.yaml \
  up -d --build
```

Use this configuration to:

- Build images from the local source code
- Test uncommitted or unpublished changes
- Verify production Dockerfiles locally

### GHCR Deployment

```bash
docker compose \
  --env-file .env.deploy \
  -p jihunpage-deploy \
  -f compose.deploy.yaml \
  up -d
```

Use this configuration to:

- Run previously published production images
- Deploy immutable commit SHA versions
- Avoid building application code on the deployment server
- Test the same images that will be used by remote servers

## Current Limitations

The current deployment environment does not include:

- AWS EC2 provisioning
- Remote server deployment
- GitHub Actions CD
- Domain configuration
- HTTPS certificates
- Production deployment approvals
- Automatic remote rollback
- External object storage for gallery files
- Managed MySQL or Redis services
