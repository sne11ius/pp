#!/usr/bin/env sh

set -e

# build backend
cd ..
docker build -f api/src/main/docker/Dockerfile.distroless-test -t pp/api-test .
docker run --rm --name pp-test --detach -p 31337:8080 pp/api-test

# build frontend
cd client
go build -cover -o pp

# run integration tests
cd ../integration-test
npm ci
rm -rf covdatafiles && mkdir covdatafiles
export GOCOVERDIR=covdatafiles
npm run test

# prepare coverage data for codecov upload
go tool covdata textfmt -i=covdatafiles -o=coverage.out

# There are some lines we don't really need to cover
# Example: everything with `log.Fatalf` is fair game. These are errors we will never attempt to handle. Testing such
# lines would be more hassle than it's worth.
# Golang/codecov has no facilities to ignore certain lines, so we just invent our own marker comment
# `// ignore.coverage` and get rid of all lines containing such comments. "Not the lines you are looking for."

temp_coverage=$(mktemp)
tail -n +2 coverage.out | while IFS= read -r line; do
    full_file=$(echo "$line" | cut -d ':' -f 1)
    details=$(echo "$line" | cut -d ':' -f 2-)

    file=$(echo "$full_file" | sed 's|.*\/\(client\/.*\)|\1|')

    ignore=false

    start_line=$(echo "$details" | cut -d ',' -f 1 | cut -d '.' -f 1)
    end_line=$(echo "$details" | cut -d ',' -f 2 | cut -d '.' -f 1)

    i="$start_line"
    while [ "$i" -le "$end_line" ]; do
       if sed "${i}q;d" "../$file" | grep -q "// ignore.coverage"; then
            ignore=true
            break
        fi
        i=$((i + 1))
    done

    if [ "$ignore" = true ]; then
        # Replace the range with a dummy coverage indicating 100% coverage
        dummy_coverage="$full_file:$start_line.0,$end_line.0 1 1"
        echo "$dummy_coverage" >> "$temp_coverage"
    else
        echo "$full_file:$details" >> "$temp_coverage"
    fi
done

echo "mode: set" > coverage.out
cat "$temp_coverage" >> coverage.out

# Remove the temporary file
rm "$temp_coverage"

docker stop pp-test
