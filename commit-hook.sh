#!/usr/bin/env sh

# Verify commit message conforms to the guidelines
# see .conform.yaml for the current settings
docker run --rm -v "${PWD}":/src -w /src \
  ghcr.io/siderolabs/conform:v0.1.0-alpha.22 enforce --commit-msg-file "$1"

# stash any unstaged changes
git stash -q --keep-index

# Verify markdown files conform to the guidelines
git ls-files | grep ".*\.md$" | xargs docker run --rm \
  -v "${PWD}":/data markdownlint/markdownlint
MDL_RESULT=$?

# Verify markdown files conform to the guidelines
git ls-files | grep ".*\.sh$" | xargs docker run --rm \
  -v "${PWD}":/mnt koalaman/shellcheck:stable
SHELLCHECK_RESULT=$?

# Check backend code
# run spotlessCheck via gradle
echo Checking api code in "${PWD}"
cd api && ./gradlew spotlessCheck --daemon
SPOTLESS_RESULT=$?

# Now do the same for detekt
./gradlew detekt
DETEKT_RESULT=$?

# Check client code with golangi
cd ../client || exit 1
echo Checking client code in "${PWD}"
docker run  --rm -v "${PWD}:/workspace" -w "/workspace/." \
  golangci/golangci-lint golangci-lint run --fix
GOLANGCI_RESULT=$?

cd ..

# Check yaml files
docker run --rm -v "${PWD}:/data" cytopia/yamllint:latest --strict .
YAML_RESULT=$?

# unstash the stashed changes
git stash pop -q

# Check exit codes
RED='\033[0;31m'
if [ $MDL_RESULT -ne 0 ]; then
    printf "%bCheck Failed for markdown\n" "${RED}"
    exit $MDL_RESULT;
elif [ $SHELLCHECK_RESULT -ne 0 ]; then
    printf "%bCheck Failed for shellcheck\n" "${RED}"
    exit $SHELLCHECK_RESULT;
elif [ $SPOTLESS_RESULT -ne 0 ]; then
    printf "%bCheck Failed for spotless\n" "${RED}"
    exit $SPOTLESS_RESULT;
elif [ $DETEKT_RESULT -ne 0 ]; then
    printf "%bCheck Failed for detekt\n" "${RED}"
    exit $DETEKT_RESULT;
elif [ $GOLANGCI_RESULT -ne 0 ]; then
    printf "%bCheck Failed for golangci\n" "${RED}"
    exit $GOLANGCI_RESULT;
elif [ $YAML_RESULT -ne 0 ]; then
    printf "%bCheck Failed for yml\n" "${RED}"
    exit $YAML_RESULT;
fi
