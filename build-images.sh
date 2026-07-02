#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME="${1:-bank-dev}"
SERVICES=("bank-accounts" "bank-cash" "bank-transfer" "bank-notifications" "bank-gateway" "bank-front-ui")

REBUILD="${REBUILD:-false}"

if ! command -v docker &>/dev/null; then
    echo "docker not found"
    exit 1
fi

if ! command -v kind &>/dev/null; then
    echo "kind not found"
    exit 1
fi

if ! kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
    echo "cluster ${CLUSTER_NAME} not found."
    exit 1
fi

# если пересобрать нужно
if [ "${REBUILD}" = "true" ] || [ ! "$(docker images -q bank:builder 2>/dev/null)" ]; then
    echo "rebuilding"
    docker build --target builder -t bank:builder -f Dockerfile .
fi

FAILED=()
for svc in "${SERVICES[@]}"; do
    echo "Building ${svc} ... "
    if docker build --target "${svc}" -t "${svc}:latest" -f Dockerfile . ; then
        echo "OK"
    else
        echo "FAILED"
        FAILED+=("${svc}")
    fi
done

if [ ${#FAILED[@]} -gt 0 ]; then
    echo "failed to build: ${FAILED[*]}"
    exit 1
fi

for svc in "${SERVICES[@]}"; do
    echo "Loading ${svc}:latest"
    kind load docker-image "${svc}:latest" --name "${CLUSTER_NAME}"
done