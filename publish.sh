#!/usr/bin/env sh

set -e

cd client && go build -trimpath -ldflags '-w -s' -o pp
upx --ultra-brute --overlay=strip --strip-relocs=0 pp

cd ..

docker build -f api/src/main/docker/Dockerfile.distroless -t ghcr.io/sne11ius/pp-api:latest -t ghcr.io/sne11ius/pp-api:"$1" .
docker push ghcr.io/sne11ius/pp-api --all-tags
