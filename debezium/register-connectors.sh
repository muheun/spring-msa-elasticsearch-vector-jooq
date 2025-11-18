#!/bin/sh
set -e

apk add --no-cache curl jq >/dev/null

echo "Waiting for Debezium Connect to be ready..."
sleep 10

CONFIG_DIR="${CONNECTOR_CONFIG_DIR:-/connectors}"

if ! ls "${CONFIG_DIR}"/*.json >/dev/null 2>&1; then
  echo "No connector config files found in ${CONFIG_DIR}"
  exit 0
fi

for CONNECTOR_FILE in "${CONFIG_DIR}"/*.json; do
  [ -e "${CONNECTOR_FILE}" ] || continue
  NAME=$(jq -r '.name' "${CONNECTOR_FILE}")
  if [ -z "${NAME}" ] || [ "${NAME}" = "null" ]; then
    echo "Skipping ${CONNECTOR_FILE}: missing connector name"
    continue
  fi
  CONFIG=$(jq -c '.config' "${CONNECTOR_FILE}")
  if [ -z "${CONFIG}" ] || [ "${CONFIG}" = "null" ]; then
    echo "Skipping ${CONNECTOR_FILE}: missing config"
    continue
  fi
  if curl -sf "http://debezium-connect:8083/connectors/${NAME}" >/dev/null; then
    echo "Updating Debezium connector ${NAME}"
    printf '%s' "${CONFIG}" | curl -s -X PUT \
      "http://debezium-connect:8083/connectors/${NAME}/config" \
      -H "Content-Type: application/json" \
      --data @- >/dev/null
  else
    echo "Registering Debezium connector ${NAME}"
    curl -s -X POST "http://debezium-connect:8083/connectors" \
      -H "Content-Type: application/json" \
      --data @"${CONNECTOR_FILE}" >/dev/null
  fi
  echo "Done processing ${NAME}"
  sleep 1
done

echo "Debezium connector registration completed!"
