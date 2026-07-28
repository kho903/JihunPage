#!/usr/bin/env bash

set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/detect-environment.sh [active|inactive|all]

Examples:
  ./scripts/detect-environment.sh
  ./scripts/detect-environment.sh active
  ./scripts/detect-environment.sh inactive
  ./scripts/detect-environment.sh all

Output:
  active    Print only the active environment
  inactive  Print only the inactive environment
  all       Print both environments

Environment variables:
  ACTIVE_BACKEND_FILE
    Override the active upstream configuration file path.
    This is mainly useful for testing.
EOF
}

if [[ $# -gt 1 ]]; then
  usage >&2
  exit 2
fi

output_mode="${1:-all}"

case "$output_mode" in
  active | inactive | all)
    ;;
  *)
    echo "Error: output mode must be 'active', 'inactive', or 'all'." >&2
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

active_backend_file="${ACTIVE_BACKEND_FILE:-${project_root}/nginx/upstreams/active-backend.conf}"

if [[ ! -f "$active_backend_file" ]]; then
  echo "Error: active backend configuration file was not found." >&2
  echo "Path: ${active_backend_file}" >&2
  exit 1
fi

detected_environments=()

while IFS= read -r detected_environment; do
  if [[ -n "$detected_environment" ]]; then
    detected_environments+=("$detected_environment")
  fi
done < <(
  sed -nE \
    's/^[[:space:]]*server[[:space:]]+backend-(blue|green):8080[[:space:]]*;[[:space:]]*$/\1/p' \
    "$active_backend_file"
)

if (( ${#detected_environments[@]} == 0 )); then
  echo "Error: no active Blue-Green backend was found." >&2
  echo "Path: ${active_backend_file}" >&2
  exit 1
fi

if (( ${#detected_environments[@]} > 1 )); then
  echo "Error: multiple active Blue-Green backends were found." >&2
  echo "Path: ${active_backend_file}" >&2
  exit 1
fi

active_environment="${detected_environments[0]}"

case "$active_environment" in
  blue)
    inactive_environment="green"
    ;;
  green)
    inactive_environment="blue"
    ;;
  *)
    echo "Error: unsupported active backend '${active_environment}'." >&2
    exit 1
    ;;
esac

case "$output_mode" in
  active)
    printf '%s\n' "$active_environment"
    ;;

  inactive)
    printf '%s\n' "$inactive_environment"
    ;;

  all)
    printf 'Active environment: %s\n' "$active_environment"
    printf 'Inactive environment: %s\n' "$inactive_environment"
    ;;
esac
