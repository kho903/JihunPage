# Blue-Green Deployment Automation

## Overview

The project provides Shell scripts that automate the local production-oriented Blue-Green deployment process.

The scripts detect the active backend, recreate the inactive backend, verify its health, switch Nginx traffic, and support rollback.

```text
Detect active backend
-> Select inactive backend
-> Build and recreate inactive backend
-> Run health check
-> Switch Nginx traffic
-> Verify the public endpoint
```

## Scripts

```text
scripts/
├── health-check.sh
├── detect-environment.sh
├── switch-backend.sh
├── deploy.sh
└── rollback.sh
```

### `health-check.sh`

Checks a specific backend directly through its host port.

```bash
./scripts/health-check.sh blue
./scripts/health-check.sh green
```

Custom retry settings:

```bash
./scripts/health-check.sh green 15 2
```

Arguments:

```text
green   -> target environment
15      -> maximum attempts
2       -> interval in seconds
```

The script verifies:

- The backend responds successfully
- The `/api/health` endpoint is available
- The `X-Backend-Instance` header matches the requested environment

### `detect-environment.sh`

Reads the active Nginx upstream configuration and detects the active and inactive environments.

```bash
./scripts/detect-environment.sh
```

Example output:

```text
Active environment: blue
Inactive environment: green
```

Print only the active environment:

```bash
./scripts/detect-environment.sh active
```

Print only the inactive environment:

```bash
./scripts/detect-environment.sh inactive
```

### `switch-backend.sh`

Switches Nginx traffic to a specified backend.

```bash
./scripts/switch-backend.sh green
```

The script performs the following steps:

```text
Check the target backend
-> Update active-backend.conf
-> Validate Nginx configuration
-> Reload Nginx
-> Verify the public endpoint
```

When switching fails, the script attempts to restore the previous Nginx upstream.

### `deploy.sh`

Automatically deploys the currently inactive backend.

```bash
./scripts/deploy.sh
```

When Blue is active:

```text
Blue remains active
-> Green is rebuilt and recreated
-> Green health check runs
-> Traffic switches to Green
```

When Green is active:

```text
Green remains active
-> Blue is rebuilt and recreated
-> Blue health check runs
-> Traffic switches to Blue
```

Only the inactive backend is recreated.

MySQL, Redis, the frontend, Nginx, and the active backend remain running.

### `rollback.sh`

Switches traffic back to the currently inactive backend.

```bash
./scripts/rollback.sh
```

The rollback target must already be running and healthy.

```text
Detect inactive backend
-> Verify rollback target
-> Switch Nginx traffic
-> Confirm active environment
```

The script does not rebuild the rollback target.

## Prerequisites

The production-oriented Docker environment must be running.

```bash
docker compose \
  -p jihunpage-prod \
  -f compose.prod.yaml \
  up -d
```

Check the containers:

```bash
docker compose \
  -p jihunpage-prod \
  -f compose.prod.yaml \
  ps
```

The following services should be available:

```text
mysql
redis
backend-blue
backend-green
frontend
nginx
```

The scripts must be executable.

```bash
chmod +x scripts/*.sh
```

## Deployment

Check the current environment:

```bash
./scripts/detect-environment.sh
```

Run the deployment:

```bash
./scripts/deploy.sh
```

Verify the result:

```bash
./scripts/detect-environment.sh

curl -si http://localhost/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

## Rollback

Run rollback:

```bash
./scripts/rollback.sh
```

Verify the result:

```bash
./scripts/detect-environment.sh

curl -si http://localhost/api/health \
  | grep -Ei 'HTTP/|x-backend-instance:'
```

## Shared Session Verification

The Blue and Green backends share the same Redis session storage.

The expected flow is:

```text
Log in through the active backend
-> Receive a SESSION cookie
-> Deploy or roll back
-> Send the same SESSION cookie
-> Authentication remains valid
```

The deployment scripts do not recreate MySQL or Redis, so existing session data remains available after switching traffic.

## Environment Variables

### Common Compose settings

```text
COMPOSE_PROJECT_NAME
COMPOSE_FILE
```

Default values:

```text
COMPOSE_PROJECT_NAME=jihunpage-prod
COMPOSE_FILE=<project-root>/compose.prod.yaml
```

### Deployment health check

```text
DEPLOY_MAX_ATTEMPTS
DEPLOY_INTERVAL_SECONDS
```

Example:

```bash
DEPLOY_MAX_ATTEMPTS=30 \
DEPLOY_INTERVAL_SECONDS=2 \
  ./scripts/deploy.sh
```

### Rollback health check

```text
ROLLBACK_MAX_ATTEMPTS
ROLLBACK_INTERVAL_SECONDS
```

Example:

```bash
ROLLBACK_MAX_ATTEMPTS=15 \
ROLLBACK_INTERVAL_SECONDS=1 \
  ./scripts/rollback.sh
```

### Public endpoint verification

```text
PUBLIC_BASE_URL
VERIFY_MAX_ATTEMPTS
VERIFY_INTERVAL_SECONDS
```

Example:

```bash
PUBLIC_BASE_URL=http://localhost \
VERIFY_MAX_ATTEMPTS=15 \
VERIFY_INTERVAL_SECONDS=1 \
  ./scripts/switch-backend.sh green
```

## Failure Behavior

### Target backend build failure

```text
Inactive backend build fails
-> Deployment stops
-> Active backend remains unchanged
```

### Target health-check failure

```text
Inactive backend health check fails
-> Nginx upstream is not changed
-> Existing backend continues receiving traffic
```

### Nginx validation failure

```text
nginx -t fails
-> Nginx is not reloaded
-> Previous upstream configuration is restored
```

### Public verification failure

```text
Nginx reload completes
-> Public endpoint does not reach the expected backend
-> Previous upstream configuration is restored
```

### Rollback target failure

```text
Rollback target is unhealthy
-> Rollback stops
-> Current backend remains active
```

## Exit Codes

```text
0 -> Successful execution
1 -> Runtime, health-check, or deployment failure
2 -> Invalid arguments or configuration values
```

## Current Limitations

The inactive backend is treated as the rollback target.

The current scripts do not yet store:

- Deployment history
- Previous Docker image tags
- Deployment timestamps
- Git commit information
- Automatic rollback based on application metrics
- Remote registry image versions

Accurate version-based rollback will be added after versioned Docker images and a deployment registry are introduced.
