#!/usr/bin/env sh

# Verify commit message conforms to the guidelines
# see .conform.yaml for the current settings
docker run --rm -v "${PWD}":/src -w /src \
  ghcr.io/siderolabs/conform:v0.1.0-alpha.22 enforce --commit-msg-file "$1"

# stash any unstaged changes because we don't want to check any changes that are not part of this commit
git stash -q --keep-index --include-untracked

# Lint markdown files
git ls-files | grep ".*\.md$" | xargs docker run --rm \
  -v "${PWD}":/data markdownlint/markdownlint
MDL_RESULT=$?

# Lint shell scripts
git ls-files | grep ".*\.sh$" | xargs docker run --rm \
  -v "${PWD}":/mnt koalaman/shellcheck:stable
SHELLCHECK_RESULT=$?

# Lint yaml files
docker run --rm -v "${PWD}:/data" cytopia/yamllint:latest --strict .
YAML_RESULT=$?

# Lint docker files
git ls-files | \
  grep '.*Dockerfile.*' | \
  xargs -I'{}' docker run --rm -i -v "${PWD}"/{}:/{} -e HADOLINT_FAILURE_THRESHOLD=style hadolint/hadolint hadolint {}
HADOLINT_RESULT=$?

# Check backend code
# run spotlessCheck via gradle
echo Checking api code in "${PWD}"
cd api && ./gradlew spotlessCheck --daemon
SPOTLESS_RESULT=$?

# Now run detekt
./gradlew detekt
DETEKT_RESULT=$?

# Check client code with golangi
cd ../client || exit 1
echo Checking client code in "${PWD}"
docker run  --rm -v "${PWD}:/workspace" -w "/workspace/." \
  golangci/golangci-lint golangci-lint run
GOLANGCI_RESULT=$?

cd ..

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
elif [ $HADOLINT_RESULT -ne 0 ]; then
    printf "%bCheck Failed for hadolint\n" "${RED}"
    exit $HADOLINT_RESULT;
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
