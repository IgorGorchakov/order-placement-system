#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/../../../.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMEOUT="${TIMEOUT:-300}"

cd "$PROJECT_ROOT"

echo "========================================="
echo "  E-Bus Platform Integration Test Runner"
echo "========================================="

# --- Build shared Maven stage once, then start services ---
echo ""
echo "[1/4] Building all services..."
echo "  Pre-building shared Maven stage (downloads dependencies once)..."
docker build --target build -t ebus-build .
echo "  Starting Docker Compose stack..."
docker-compose up -d --build

# --- Wait for all services to be healthy ---
echo ""
echo "[2/4] Waiting for services to become healthy (timeout: ${TIMEOUT}s)..."

services=(user-service search-service booking-manager payment-service fulfillment-service)
elapsed=0

for service in "${services[@]}"; do
    while true; do
        if [ "$elapsed" -ge "$TIMEOUT" ]; then
            echo "ERROR: Timed out waiting for services to become healthy."
            echo "Dumping docker-compose logs..."
            docker-compose logs --tail=50
            exit 1
        fi

        status=$(docker-compose ps --format json | grep -o "\"$service\"[^}]*" | grep -o '"healthy"' || true)
        if [ -n "$status" ]; then
            echo "  $service: healthy"
            break
        fi

        sleep 5
        elapsed=$((elapsed + 5))
    done
done

echo "All services are healthy!"

# --- Run integration tests ---
echo ""
echo "[3/4] Running integration tests..."
cd "$PROJECT_ROOT/integration-tests"
mvn verify -Dbase.url="$BASE_URL"
TEST_EXIT=$?

# --- Summary ---
echo ""
echo "========================================="
if [ "$TEST_EXIT" -eq 0 ]; then
    echo "  ALL TESTS PASSED"
else
    echo "  TESTS FAILED (exit code: $TEST_EXIT)"
fi
echo "========================================="

exit $TEST_EXIT
