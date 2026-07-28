#!/usr/bin/env bash

set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/health-check.sh <blue|green> [max-attempts] [interval-seconds]

Examples:
  ./scripts/health-check.sh blue
  ./scripts/health-check.sh green 15 2

Arguments:
  environment       Backend environment to check: blue or green
  max-attempts      Maximum number of attempts. Default: 10
  interval-seconds  Delay between attempts. Default: 3
EOF
}

if [[ $# -lt 1 || $# -gt 3 ]]; then
  usage
  exit 2
fi

environment="$1"
max_attempts="${2:-10}"
interval_seconds="${3:-3}"

case "$environment" in
  blue)
    port="8081"
    expected_instance="backend-blue"
    ;;

  green)
    port="8082"
    expected_instance="backend-green"
    ;;

  *)
    echo "Error: environment must be either 'blue' or 'green'." >&2
    echo >&2
    usage >&2
    exit 2
    ;;
esac

if ! [[ "$max_attempts" =~ ^[1-9][0-9]*$ ]]; then
  echo "Error: max-attempts must be a positive integer." >&2
  exit 2
fi

if ! [[ "$interval_seconds" =~ ^[0-9]+$ ]]; then
  echo "Error: interval-seconds must be a non-negative integer." >&2
  exit 2
fi

base_url="${HEALTH_CHECK_BASE_URL:-http://localhost}"
health_url="${base_url}:${port}/api/health"

headers_file="$(mktemp)"

cleanup() {
  rm -f "$headers_file"
}

trap cleanup EXIT

echo "Checking ${expected_instance}"
echo "URL: ${health_url}"
echo "Maximum attempts: ${max_attempts}"
echo

attempt=1

while (( attempt <= max_attempts )); do
  : > "$headers_file"

  echo "Health check attempt ${attempt}/${max_attempts}..."

  if curl \
    --silent \
    --show-error \
    --fail \
    --connect-timeout 3 \
    --max-time 5 \
    --dump-header "$headers_file" \
    --output /dev/null \
    "$health_url"; then

    if tr -d '\r' < "$headers_file" \
      | grep -qi \
        "^X-Backend-Instance:[[:space:]]*${expected_instance}[[:space:]]*$"; then

      echo "Health check passed: ${expected_instance}"
      exit 0
    fi

    actual_instance="$(
      tr -d '\r' < "$headers_file" \
        | grep -i '^X-Backend-Instance:' \
        | head -n 1 \
        | cut -d ':' -f 2- \
        | xargs \
        || true
    )"

    if [[ -z "$actual_instance" ]]; then
      actual_instance="<missing header>"
    fi

    echo "Unexpected backend instance: ${actual_instance}" >&2
  else
    echo "Backend is not ready yet." >&2
  fi

  if (( attempt < max_attempts )); then
    sleep "$interval_seconds"
  fi

  attempt=$((attempt + 1))
done

echo >&2
echo "Health check failed: ${expected_instance}" >&2
echo "URL: ${health_url}" >&2
exit 1
