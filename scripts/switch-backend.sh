#!/usr/bin/env bash

set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/switch-backend.sh <blue|green>

Examples:
  ./scripts/switch-backend.sh green
  ./scripts/switch-backend.sh blue

Environment variables:
  COMPOSE_PROJECT_NAME
    Docker Compose project name.
    Default: jihunpage-prod

  COMPOSE_FILE
    Production Docker Compose file path.
    Default: <project-root>/compose.prod.yaml

  PUBLIC_BASE_URL
    Public Nginx URL used after switching.
    Default: http://localhost

  VERIFY_MAX_ATTEMPTS
    Maximum number of verification attempts.
    Default: 10

  VERIFY_INTERVAL_SECONDS
    Delay between verification attempts.
    Default: 1
EOF
}

if [[ $# -ne 1 ]]; then
  usage >&2
  exit 2
fi

target_environment="$1"

case "$target_environment" in
  blue | green)
    ;;
  *)
    echo "Error: target environment must be 'blue' or 'green'." >&2
    echo >&2
    usage >&2
    exit 2
    ;;
esac

script_directory="$(
  cd -- "$(dirname -- "${BASH_SOURCE[0]}")" \
    && pwd
)"

project_root="$(
  cd -- "${script_directory}/.." \
    && pwd
)"

health_check_script="${script_directory}/health-check.sh"
detect_environment_script="${script_directory}/detect-environment.sh"

active_backend_file="${project_root}/nginx/upstreams/active-backend.conf"
target_backend_file="${project_root}/nginx/upstreams/backend-${target_environment}.conf"

compose_project_name="${COMPOSE_PROJECT_NAME:-jihunpage-prod}"
compose_file="${COMPOSE_FILE:-${project_root}/compose.prod.yaml}"

public_base_url="${PUBLIC_BASE_URL:-http://localhost}"
public_health_url="${public_base_url%/}/api/health"

verify_max_attempts="${VERIFY_MAX_ATTEMPTS:-10}"
verify_interval_seconds="${VERIFY_INTERVAL_SECONDS:-1}"

expected_instance="backend-${target_environment}"

if ! [[ "$verify_max_attempts" =~ ^[1-9][0-9]*$ ]]; then
  echo "Error: VERIFY_MAX_ATTEMPTS must be a positive integer." >&2
  exit 2
fi

if ! [[ "$verify_interval_seconds" =~ ^[0-9]+$ ]]; then
  echo "Error: VERIFY_INTERVAL_SECONDS must be a non-negative integer." >&2
  exit 2
fi

required_files="
${health_check_script}
${detect_environment_script}
${active_backend_file}
${target_backend_file}
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

current_environment="$(
  "$detect_environment_script" active
)"

if [[ "$current_environment" == "$target_environment" ]]; then
  echo "The requested environment is already active."
  echo "Active environment: ${current_environment}"
  exit 0
fi

backup_file="$(mktemp)"

cleanup() {
  rm -f "$backup_file"
}

trap cleanup EXIT

cp "$active_backend_file" "$backup_file"

compose_command() {
  docker compose \
    -p "$compose_project_name" \
    -f "$compose_file" \
    "$@"
}

restore_previous_environment() {
  echo
  echo "Restoring previous environment: ${current_environment}" >&2

  cp "$backup_file" "$active_backend_file"

  if compose_command exec -T nginx nginx -t; then
    compose_command exec -T nginx nginx -s reload
    echo "Previous environment restored: ${current_environment}" >&2
  else
    echo "Error: failed to validate the restored Nginx configuration." >&2
    echo "Manual recovery may be required." >&2
  fi
}

verify_public_backend() {
  attempt=1

  while (( attempt <= verify_max_attempts )); do
    headers_file="$(mktemp)"

    echo "Public verification attempt ${attempt}/${verify_max_attempts}..."

    if curl \
      --silent \
      --show-error \
      --fail \
      --connect-timeout 3 \
      --max-time 5 \
      --dump-header "$headers_file" \
      --output /dev/null \
      "$public_health_url"; then

      if tr -d '\r' < "$headers_file" \
        | grep -qi \
          "^X-Backend-Instance:[[:space:]]*${expected_instance}[[:space:]]*$"; then

        rm -f "$headers_file"
        return 0
      fi
    fi

    actual_instance="$(
      tr -d '\r' < "$headers_file" \
        | grep -i '^X-Backend-Instance:' \
        | head -n 1 \
        | cut -d ':' -f 2- \
        | xargs \
        || true
    )"

    rm -f "$headers_file"

    if [[ -z "$actual_instance" ]]; then
      actual_instance="<missing header>"
    fi

    echo "Expected: ${expected_instance}" >&2
    echo "Actual: ${actual_instance}" >&2

    if (( attempt < verify_max_attempts )); then
      sleep "$verify_interval_seconds"
    fi

    attempt=$((attempt + 1))
  done

  return 1
}

echo "Current environment: ${current_environment}"
echo "Target environment: ${target_environment}"
echo

echo "Step 1/5: Checking target backend..."
"$health_check_script" "$target_environment"

echo
echo "Step 2/5: Updating the active Nginx upstream..."
cp "$target_backend_file" "$active_backend_file"

echo
echo "Step 3/5: Validating the Nginx configuration..."

if ! compose_command exec -T nginx nginx -t; then
  echo "Error: Nginx configuration validation failed." >&2
  restore_previous_environment
  exit 1
fi

echo
echo "Step 4/5: Reloading Nginx..."

if ! compose_command exec -T nginx nginx -s reload; then
  echo "Error: Nginx reload failed." >&2
  restore_previous_environment
  exit 1
fi

echo
echo "Step 5/5: Verifying the public backend..."

if ! verify_public_backend; then
  echo "Error: public backend verification failed." >&2
  restore_previous_environment
  exit 1
fi

echo
echo "Traffic switch completed successfully."
echo "Previous environment: ${current_environment}"
echo "Active environment: ${target_environment}"
echo "Public backend: ${expected_instance}"