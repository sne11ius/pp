#!/usr/bin/env sh

set -e

docker build -f src/main/docker/Dockerfile.distroless -t ghcr.io/sne11ius/pp-api:latest -t ghcr.io/sne11ius/pp-api:"$1" .
docker push ghcr.io/sne11ius/pp-api --all-tags
