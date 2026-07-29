# GitHub Actions CI

## Overview

The project uses GitHub Actions to automatically validate backend, frontend, and shell script changes.

The CI workflow runs when:

- A pull request targets the `main` branch
- A commit is pushed directly to the `main` branch

The workflow file is:

```text
.github/workflows/ci.yaml
```

## Workflow Jobs

```text
CI
├── Shell Check
├── Frontend Check
└── Backend Test
```

Each job runs independently on a separate GitHub-hosted Ubuntu runner.

A pull request should be merged only after all required jobs pass.

## Shell Check

The Shell Check job validates every script under:

```text
scripts/*.sh
```

It performs two checks.

### Bash syntax

```bash
for script in scripts/*.sh; do
    bash -n "${script}"
done
```

The syntax check detects issues such as:

- Missing `fi` or `done`
- Invalid condition syntax
- Unclosed quotes
- Invalid Here Document syntax

The scripts are not actually executed during this step.

### Execution permission

```bash
for script in scripts/*.sh; do
    [[ -x "${script}" ]]
done
```

All deployment scripts must remain executable.

Expected Git file mode:

```text
100755
```

A script without execution permission causes the job to fail.

## Frontend Check

The Frontend Check job runs inside the `frontend` directory.

```text
Set up Node.js
-> Restore npm cache
-> npm ci
-> npm run lint
-> npm run build
```

### Dependency installation

```bash
npm ci
```

The command installs dependencies using `package-lock.json`.

It is used instead of `npm install` so that CI uses a reproducible dependency tree.

### ESLint

```bash
npm run lint
```

The job fails when ESLint detects an error.

### Production build

```bash
npm run build
```

The command creates the Vite production build under:

```text
frontend/dist
```

The generated `dist` directory is not committed.

## Backend Test

The Backend Test job runs the isolated Docker test environment.

```text
backend-test
├── mysql-test
└── redis-test
```

The test environment is activated through the Compose `test` profile.

```bash
docker compose \
  -p jihunpage-ci \
  --profile test \
  up \
  --build \
  --abort-on-container-exit \
  --exit-code-from backend-test \
  backend-test
```

The backend tests use:

- An isolated MySQL test container
- An isolated Redis test container
- Temporary test data
- The Spring `test` profile

The job result follows the exit code of the `backend-test` container.

```text
Backend tests pass
-> backend-test exits with code 0
-> GitHub Actions job passes

Backend tests fail
-> backend-test exits with a non-zero code
-> GitHub Actions job fails
```

## Test Environment Variables

The workflow uses temporary test credentials inside the GitHub Actions runner.

```text
MYSQL_DATABASE
MYSQL_USER
MYSQL_PASSWORD
MYSQL_ROOT_PASSWORD
SPRING_DATASOURCE_PASSWORD
```

These are test-only values and are not production credentials.

GitHub repository secrets are not required for the current CI workflow.

## Cleanup

The backend cleanup step always runs.

```yaml
if: always()
```

Cleanup command:

```bash
docker compose \
  -p jihunpage-ci \
  --profile test \
  down \
  --volumes \
  --remove-orphans
```

This removes the CI test containers, network, and temporary volumes even when the test step fails.

## Local Verification

The same checks can be run locally before pushing a branch.

### Shell Scripts

```bash
for script in scripts/*.sh; do
    echo "Checking syntax: ${script}"
    bash -n "${script}"
done
```

Check execution permissions:

```bash
for script in scripts/*.sh; do
  if [[ -x "${script}" ]]; then
    echo "Executable: ${script}"
  else
    echo "Not executable: ${script}"
  fi
done
```

### Frontend

```bash
cd frontend

npm ci
npm run lint
npm run build

cd ..
```

### Backend

```bash
docker compose \
  -p jihunpage-ci \
  --profile test \
  up \
  --build \
  --abort-on-container-exit \
  --exit-code-from backend-test \
  backend-test
```

Cleanup:

```bash
docker compose \
  -p jihunpage-ci \
  --profile test \
  down \
  --volumes \
  --remove-orphans
```

## Pull Request Verification

After pushing the feature branch and creating a pull request targeting `main`, GitHub Actions starts the CI workflow automatically.

Expected checks:

```text
Shell Check
Frontend Check
Backend Test
```

Each check should complete successfully before merge.

When a job fails:

1. Open the failed check from the pull request
2. Open the failed step
3. Review the command output
4. Reproduce the same command locally
5. Fix the issue
6. Commit and push the correction

The pull request workflow runs again after the new commit is pushed.

## Permissions

The workflow uses read-only repository permissions.

```yaml
permissions:
  contents: read
```

The current CI workflow can read the checked-out repository but cannot modify repository contents, publish releases, or deploy applications.

## Current Limitations

The current workflow validates the project but does not deploy it.

It does not yet include:

- Docker image publishing
- Container registry authentication
- Versioned image tags
- Automatic Blue-Green deployment
- AWS deployment
- GitHub Actions CD
- Release creation
- Production secrets
- Security audit enforcement
- Code coverage reporting
