#!/bin/sh

SERVICE_HOST=${1:-"localhost:8080"}

cat pipeline.yaml| base64 | curl -X POST -H "Content-Type: application/json"  --verbose --data-binary @- ${SERVICE_HOST}/api/v1/pipeline