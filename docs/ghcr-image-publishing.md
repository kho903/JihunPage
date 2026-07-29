# GHCR Image Publishing

## Overview

The project uses GitHub Actions to build and publish production Docker images to GitHub Container Registry.

The publishing workflow file is:

```text
.github/workflows/publish-images.yaml
```

The workflow publishes separate images for the backend and frontend.

```text
GitHub Container Registry
├── jihunpage-backend
└── jihunpage-frontend
```

## Published Images

### Backend

```text
ghcr.io/kho903/jihunpage-backend
```

The backend image contains:

- Java 21 runtime
- Spring Boot executable JAR
- Production backend application
- Non-root application user

### Frontend

```text
ghcr.io/kho903/jihunpage-frontend
```

The frontend image contains:

- Vite production build
- React static files
- Nginx web server
- SPA routing configuration

## Supported Platforms

Both production images support the following Linux platforms:

```text
linux/amd64
linux/arm64
```

The published image tags point to multi-platform manifests.

```text
ghcr.io/kho903/jihunpage-backend:latest
├── linux/amd64
└── linux/arm64

ghcr.io/kho903/jihunpage-frontend:latest
├── linux/amd64
└── linux/arm64
```

Docker automatically selects the appropriate image for the host architecture.

```text
Intel and AMD x86-64 systems
→ linux/amd64

Apple Silicon and ARM64 systems
→ linux/arm64
```

The GitHub Actions workflow uses QEMU and Docker Buildx to build both platforms.

```yaml
- name: Set up QEMU
  uses: docker/setup-qemu-action@v4

- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v4
```

Each image build specifies both target platforms:

```yaml
platforms: linux/amd64,linux/arm64
```

## Publishing Flow

```text
Pull request targets main
-> CI workflow runs
-> Images are not published

Pull request is merged
-> A push occurs on main
-> CI workflow runs
-> CI completes successfully
-> Publish Docker Images workflow runs
-> Backend and frontend images are published
```

The publishing workflow is triggered after the `CI` workflow completes on the `main` branch.

```yaml
on:
  workflow_run:
    workflows:
      - CI
    types:
      - completed
    branches:
      - main
```

The publishing jobs run only when:

- The previous CI workflow succeeded
- The previous CI workflow was triggered by a push

```yaml
if: "${{ github.event.workflow_run.conclusion == 'success' && github.event.workflow_run.event == 'push' }}"
```

This prevents images from being published from pull request validation runs.

## Workflow Jobs

```text
Publish Docker Images
├── Publish Backend Image
└── Publish Frontend Image
```

The backend and frontend jobs run independently.

Each job performs the following steps:

```text
Check out tested commit
-> Set up Docker Buildx
-> Log in to GHCR
-> Generate image metadata
-> Build production image
-> Push image to GHCR
```

## Tested Commit

The publishing workflow checks out the exact commit that passed the previous CI workflow.

```yaml
ref: "${{ github.event.workflow_run.head_sha }}"
```

The same commit SHA is used for:

- Source code checkout
- Docker image tag
- OCI revision metadata

```text
CI tested commit
=
Docker build commit
=
SHA image tag
=
OCI revision
```

## Image Tags

Each image is published with two tags.

```text
latest
sha-<full-commit-sha>
```

### Latest Tag

```text
ghcr.io/kho903/jihunpage-backend:latest
ghcr.io/kho903/jihunpage-frontend:latest
```

The `latest` tag represents the most recently published image from the `main` branch.

### Commit SHA Tag

```text
ghcr.io/kho903/jihunpage-backend:sha-<full-commit-sha>
ghcr.io/kho903/jihunpage-frontend:sha-<full-commit-sha>
```

The SHA tag identifies the exact Git commit used to build the image.

Use the SHA tag when a reproducible deployment or rollback target is required.

## OCI Metadata

The published images include OCI metadata.

```text
org.opencontainers.image.source
org.opencontainers.image.revision
org.opencontainers.image.description
```

### Source

```text
https://github.com/kho903/JihunPage
```

The source label identifies the GitHub repository associated with the image.

### Revision

```text
<Git commit SHA>
```

The revision label identifies the exact commit used to build the image.

### Description

Backend:

```text
JihunPage Spring Boot backend
```

Frontend:

```text
JihunPage React frontend
```

## Authentication

The workflow logs in to GHCR using the GitHub Actions `GITHUB_TOKEN`.

```yaml
registry: ghcr.io
username: "${{ github.actor }}"
password: "${{ secrets.GITHUB_TOKEN }}"
```

The workflow permissions are:

```yaml
permissions:
  contents: read
  packages: write
```

These permissions allow the workflow to:

- Read the repository source code
- Publish container images to GitHub Packages

A personal access token is not stored in the repository.

## Docker Build Cache

The workflow uses GitHub Actions cache storage for Docker build layers.

Backend cache:

```yaml
cache-from: type=gha,scope=backend
cache-to: type=gha,mode=max,scope=backend
```

Frontend cache:

```yaml
cache-from: type=gha,scope=frontend
cache-to: type=gha,mode=max,scope=frontend
```

Separate scopes prevent backend and frontend build caches from being mixed.

## Inspect Multi-Platform Manifests

Inspect the backend image manifest:

```bash
docker buildx imagetools inspect \
  ghcr.io/kho903/jihunpage-backend:latest
```

Inspect the frontend image manifest:

```bash
docker buildx imagetools inspect \
  ghcr.io/kho903/jihunpage-frontend:latest
```

The output should include both platforms:

```text
Platform: linux/amd64
Platform: linux/arm64
```

Additional `unknown/unknown` entries may represent image provenance or attestation metadata rather than executable container platforms.

## Pull Images

### Backend Latest Image

```bash
docker pull ghcr.io/kho903/jihunpage-backend:latest
```

### Frontend Latest Image

```bash
docker pull ghcr.io/kho903/jihunpage-frontend:latest
```

### Apple Silicon Verification

On an Apple Silicon Mac, Docker should automatically pull the ARM64 image without requiring an explicit `--platform` option.

```bash
docker pull ghcr.io/kho903/jihunpage-backend:latest
docker pull ghcr.io/kho903/jihunpage-frontend:latest
```

The following workaround should no longer be necessary:

```bash
docker pull \
  --platform linux/amd64 \
  ghcr.io/kho903/jihunpage-backend:latest
```

### Specific Commit Image

```bash
docker pull \
  ghcr.io/kho903/jihunpage-backend:sha-<full-commit-sha>
```

```bash
docker pull \
  ghcr.io/kho903/jihunpage-frontend:sha-<full-commit-sha>
```

Replace `<full-commit-sha>` with the SHA displayed in the GitHub Actions workflow or package version list.

## Inspect Images

Inspect backend OCI metadata:

```bash
docker image inspect \
  ghcr.io/kho903/jihunpage-backend:latest \
  --format '{{ json .Config.Labels }}'
```

Inspect frontend OCI metadata:

```bash
docker image inspect \
  ghcr.io/kho903/jihunpage-frontend:latest \
  --format '{{ json .Config.Labels }}'
```

Check the source label only:

```bash
docker image inspect \
  ghcr.io/kho903/jihunpage-backend:latest \
  --format '{{ index .Config.Labels "org.opencontainers.image.source" }}'
```

Check the revision label only:

```bash
docker image inspect \
  ghcr.io/kho903/jihunpage-backend:latest \
  --format '{{ index .Config.Labels "org.opencontainers.image.revision" }}'
```

## Package Visibility

After the images are published for the first time, verify the visibility of both packages in the GitHub package settings.

Check the following packages:

```text
jihunpage-backend
jihunpage-frontend
```

For public image pulling, configure the package visibility appropriately in GitHub.

Private packages may require GHCR authentication before pulling.

## Authenticated Pull

Log in to GHCR when authentication is required:

```bash
echo "<personal-access-token>" | \
  docker login ghcr.io \
  --username kho903 \
  --password-stdin
```

The token must have the permissions required to read the package.

Do not commit tokens to the repository or include them directly in shell history.

## Verification After Merge

After merging the feature branch into `main`, verify the following workflow sequence:

```text
CI
├── Shell Check
├── Frontend Check
└── Backend Test

Publish Docker Images
├── Publish Backend Image
└── Publish Frontend Image
```

All CI jobs must pass before the publishing workflow builds the images.

After publication, confirm that both image repositories contain:

```text
latest
sha-<merge-commit-sha>
```

Then verify the images with:

```bash
docker pull ghcr.io/kho903/jihunpage-backend:latest
docker pull ghcr.io/kho903/jihunpage-frontend:latest
```

Verify the published manifests:

```bash
docker buildx imagetools inspect \
  ghcr.io/kho903/jihunpage-backend:latest
```

```bash
docker buildx imagetools inspect \
  ghcr.io/kho903/jihunpage-frontend:latest
```

Confirm that both outputs include:

```text
linux/amd64
linux/arm64
```

On an Apple Silicon Mac, verify native image selection:

```bash
docker pull ghcr.io/kho903/jihunpage-backend:latest
docker pull ghcr.io/kho903/jihunpage-frontend:latest
```

The commands should succeed without:

```text
--platform linux/amd64
```

## Current Limitations

The current publishing workflow does not include:

- Automatic AWS deployment
- Automatic Blue-Green traffic switching
- Semantic version tags
- Image vulnerability scan enforcement
- Container image signing
- Automatic GitHub Release creation
- Automatic package cleanup
- Production deployment approval
