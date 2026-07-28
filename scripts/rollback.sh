#!/usr/bin/env bash

set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/rollback.sh

The script automatically:

  1. Detects the currently active backend environment
  2. Selects the inactive environment as the rollback target
  3. Verifies the rollback target
  4. Switches Nginx traffic to the verified target
  5. Confirms the final active environment

Environment variables:
  COMPOSE_PROJECT_NAME
    Docker Compose project name.
    Default: jihunpage-prod

  COMPOSE_FILE
    Production Docker Compose file path.
    Default: <project-root>/compose.prod.yaml

  ROLLBACK_MAX_ATTEMPTS
    Maximum number of rollback target health-check attempts.
    Default: 10

  ROLLBACK_INTERVAL_SECONDS
    Delay between health-check attempts.
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

rollback_max_attempts="${ROLLBACK_MAX_ATTEMPTS:-10}"
rollback_interval_seconds="${ROLLBACK_INTERVAL_SECONDS:-2}"

if ! [[ "$rollback_max_attempts" =~ ^[1-9][0-9]*$ ]]; then
  echo "Error: ROLLBACK_MAX_ATTEMPTS must be a positive integer." >&2
  exit 2
fi

if ! [[ "$rollback_interval_seconds" =~ ^[0-9]+$ ]]; then
  echo "Error: ROLLBACK_INTERVAL_SECONDS must be a non-negative integer." >&2
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

current_environment="$(
  "$detect_environment_script" active
)"

rollback_environment="$(
  "$detect_environment_script" inactive
)"

current_service="backend-${current_environment}"
rollback_service="backend-${rollback_environment}"

if ! compose_command config --services \
  | grep -qx "$current_service"; then

  echo "Error: current backend service was not found in Compose." >&2
  echo "Service: ${current_service}" >&2
  exit 1
fi

if ! compose_command config --services \
  | grep -qx "$rollback_service"; then

  echo "Error: rollback backend service was not found in Compose." >&2
  echo "Service: ${rollback_service}" >&2
  exit 1
fi

rollback_container_id="$(
  compose_command ps -q "$rollback_service" 2>/dev/null \
    || true
)"

if [[ -z "$rollback_container_id" ]]; then
  echo "Error: rollback target container was not found." >&2
  echo "Service: ${rollback_service}" >&2
  echo "The current environment remains active: ${current_environment}" >&2
  exit 1
fi

echo "Blue-Green rollback"
echo "==================="
echo "Current environment: ${current_environment}"
echo "Rollback target: ${rollback_environment}"
echo "Current service: ${current_service}"
echo "Rollback service: ${rollback_service}"
echo "Rollback container: ${rollback_container_id}"
echo

echo "Step 1/3: Checking the rollback target..."

if ! "$health_check_script" \
  "$rollback_environment" \
  "$rollback_max_attempts" \
  "$rollback_interval_seconds"; then

  echo >&2
  echo "Rollback target verification failed." >&2
  echo "Nginx traffic was not changed." >&2
  echo "Current environment: ${current_environment}" >&2
  echo "Failed rollback target: ${rollback_environment}" >&2
  exit 1
fi

echo
echo "Step 2/3: Switching Nginx traffic..."

if ! COMPOSE_PROJECT_NAME="$compose_project_name" \
  COMPOSE_FILE="$compose_file" \
  "$switch_backend_script" "$rollback_environment"; then

  echo >&2
  echo "Rollback traffic switching failed." >&2
  echo "The switch script attempted to preserve or restore:" >&2
  echo "  ${current_environment}" >&2
  exit 1
fi

echo
echo "Step 3/3: Confirming the active environment..."

confirmed_environment="$(
  "$detect_environment_script" active
)"

if [[ "$confirmed_environment" != "$rollback_environment" ]]; then
  echo "Error: the final active environment is unexpected." >&2
  echo "Expected: ${rollback_environment}" >&2
  echo "Actual: ${confirmed_environment}" >&2
  exit 1
fi

echo
echo "Rollback completed successfully."
echo "Previous environment: ${current_environment}"
echo "Active environment: ${confirmed_environment}"
echo "Active service: ${rollback_service}"
echo "Container: ${rollback_container_id}"