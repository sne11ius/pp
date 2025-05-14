#!/usr/bin/env sh

# Verify commit message conforms to the guidelines
# see .conform.yaml for the current settings
docker run --rm -v "${PWD}":/src -w /src \
  ghcr.io/siderolabs/conform:v0.1.0-alpha.22 enforce --commit-msg-file "$1"
CONFORM_RESULT=$?

if [ $CONFORM_RESULT -ne 0 ]; then
    echo Check Failed for commit message
    exit $CONFORM_RESULT;
fi

# stash any unstaged changes because we don't want to check any changes that are not part of this commit
git stash -q --keep-index --include-untracked

# Lint markdown files
docker run -v "${PWD}":/workdir davidanson/markdownlint-cli2 "**/*.md"
MD_RESULT=$?

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

cd ..

# unstash the stashed changes
git stash pop -q

# Check exit codes
RED='\033[0;31m'
if [ $MD_RESULT -ne 0 ]; then
    printf "%bCheck Failed for markdown\n" "${RED}"
    exit $MD_RESULT;
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
elif [ $YAML_RESULT -ne 0 ]; then
    printf "%bCheck Failed for yml\n" "${RED}"
    exit $YAML_RESULT;
fi
