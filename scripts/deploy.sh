#!/usr/bin/env bash

set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/deploy.sh

The script automatically:

  1. Detects the active backend environment
  2. Selects the inactive environment
  3. Builds and recreates only the inactive backend
  4. Runs a direct backend health check
  5. Switches Nginx traffic to the verified backend

Environment variables:
  COMPOSE_PROJECT_NAME
    Docker Compose project name.
    Default: jihunpage-prod

  COMPOSE_FILE
    Production Docker Compose file path.
    Default: <project-root>/compose.prod.yaml

  DEPLOY_MAX_ATTEMPTS
    Maximum number of target health-check attempts.
    Default: 20

  DEPLOY_INTERVAL_SECONDS
    Delay between target health-check attempts.
    Default: 2
EOF
}

if [[ $# -ne 0 ]]; then
  usage >&2
  exit 2
fi

script_directory="$(
  cd -- "$(dirname -- "${BASH_SOURCE[0]}")" \
    && pwd
)"

project_root="$(
  cd -- "${script_directory}/.." \
    && pwd
)"

detect_environment_script="${script_directory}/detect-environment.sh"
health_check_script="${script_directory}/health-check.sh"
switch_backend_script="${script_directory}/switch-backend.sh"

compose_project_name="${COMPOSE_PROJECT_NAME:-jihunpage-prod}"
compose_file="${COMPOSE_FILE:-${project_root}/compose.prod.yaml}"

deploy_max_attempts="${DEPLOY_MAX_ATTEMPTS:-20}"
deploy_interval_seconds="${DEPLOY_INTERVAL_SECONDS:-2}"

if ! [[ "$deploy_max_attempts" =~ ^[1-9][0-9]*$ ]]; then
  echo "Error: DEPLOY_MAX_ATTEMPTS must be a positive integer." >&2
  exit 2
fi

if ! [[ "$deploy_interval_seconds" =~ ^[0-9]+$ ]]; then
  echo "Error: DEPLOY_INTERVAL_SECONDS must be a non-negative integer." >&2
  exit 2
fi

required_files="
${detect_environment_script}
${health_check_script}
${switch_backend_script}
${compose_file}
"

while IFS= read -r required_file; do
  if [[ -z "$required_file" ]]; then
    continue
  fi

  if [[ ! -f "$required_file" ]]; then
    echo "Error: required file was not found." >&2
    echo "Path: ${required_file}" >&2
    exit 1
  fi
done <<EOF
${required_files}
EOF

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker command was not found." >&2
  exit 1
fi

compose_command() {
  docker compose \
    -p "$compose_project_name" \
    -f "$compose_file" \
    "$@"
}

active_environment="$(
  "$detect_environment_script" active
)"

target_environment="$(
  "$detect_environment_script" inactive
)"

active_service="backend-${active_environment}"
target_service="backend-${target_environment}"

if ! compose_command config --services \
  | grep -qx "$active_service"; then

  echo "Error: active backend service was not found in Compose." >&2
  echo "Service: ${active_service}" >&2
  exit 1
fi

if ! compose_command config --services \
  | grep -qx "$target_service"; then

  echo "Error: target backend service was not found in Compose." >&2
  echo "Service: ${target_service}" >&2
  exit 1
fi

previous_target_container_id="$(
  compose_command ps -q "$target_service" 2>/dev/null \
    || true
)"

echo "Blue-Green deployment"
echo "====================="
echo "Active environment: ${active_environment}"
echo "Deployment target: ${target_environment}"
echo "Active service: ${active_service}"
echo "Target service: ${target_service}"
echo

echo "Step 1/4: Building and recreating the inactive backend..."

if ! compose_command up \
  -d \
  --build \
  --no-deps \
  --force-recreate \
  "$target_service"; then

  echo >&2
  echo "Error: failed to build or recreate ${target_service}." >&2
  echo "The active environment was not changed." >&2
  echo "Active environment: ${active_environment}" >&2
  exit 1
fi

new_target_container_id="$(
  compose_command ps -q "$target_service"
)"

if [[ -z "$new_target_container_id" ]]; then
  echo "Error: the target backend container was not created." >&2
  echo "The active environment was not changed." >&2
  exit 1
fi

echo "Target container: ${new_target_container_id}"

if [[ -n "$previous_target_container_id" ]]; then
  if [[ "$previous_target_container_id" == "$new_target_container_id" ]]; then
    echo "Warning: the target container ID did not change." >&2
  else
    echo "Previous target container: ${previous_target_container_id}"
  fi
fi

echo
echo "Step 2/4: Checking the deployed backend..."

if ! "$health_check_script" \
  "$target_environment" \
  "$deploy_max_attempts" \
  "$deploy_interval_seconds"; then

  echo >&2
  echo "Deployment verification failed." >&2
  echo "The active Nginx upstream was not changed." >&2
  echo "Active environment: ${active_environment}" >&2
  echo "Failed target: ${target_environment}" >&2
  echo >&2
  echo "Inspect the target logs with:" >&2
  echo "  docker compose -p ${compose_project_name} -f ${compose_file} logs --tail=200 ${target_service}" >&2
  exit 1
fi

echo
echo "Step 3/4: Switching Nginx traffic..."

if ! COMPOSE_PROJECT_NAME="$compose_project_name" \
  COMPOSE_FILE="$compose_file" \
  "$switch_backend_script" "$target_environment"; then

  echo >&2
  echo "Traffic switching failed." >&2
  echo "The switch script attempted to preserve or restore:" >&2
  echo "  ${active_environment}" >&2
  exit 1
fi

echo
echo "Step 4/4: Confirming the active environment..."

confirmed_environment="$(
  "$detect_environment_script" active
)"

if [[ "$confirmed_environment" != "$target_environment" ]]; then
  echo "Error: the final active environment is unexpected." >&2
  echo "Expected: ${target_environment}" >&2
  echo "Actual: ${confirmed_environment}" >&2
  exit 1
fi

echo
echo "Deployment completed successfully."
echo "Previous environment: ${active_environment}"
echo "Active environment: ${confirmed_environment}"
echo "Active service: ${target_service}"
echo "Container: ${new_target_container_id}"