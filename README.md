# pp - Planning Poker

Simple client/server planning poker

This project is in early development, so the documentation is geared towards
developers.

## Development

### Prerequisites

commits/pushes are checked by github actions to verify every change conforms to
our development guidelines. To make sure your changes can be merged and land in
the next release, consider running the checks on each commit.

[docker](https://www.docker.com/) is required for the suggested commit-hook. We
use docker during development so we don't need to install non-essential
tools locally on each dev machine.

Use the following script to install a git hook that checks your changes on each
commit.

  ```bash
  cat <<EOF > .git/hooks/commit-msg
  #!/bin/sh
  # Verify commit message conforms to the guidelines
  # see .conform.yaml for the current settings
  docker run --rm -v $PWD:/src -w /src \
    ghcr.io/siderolabs/conform:v0.1.0-alpha.22 enforce --commit-msg-file \$1
  # Verify markdown files conform to the guidelines
  # We use git ls-files instead of the -g option of markdownlint since the
  # option did not work as expected.
  git ls-files | grep *.md | xargs docker run --rm \
    -v ${PWD}:/data markdownlint/markdownlint
  # Check backend code
  # stash any unstaged changes
  git stash -q --keep-index
  # run the spotlessCheck with the gradle wrapper
  cd api && ./gradlew spotlessCheck --daemon
  # store the last exit code in a variable
  SPOTLESS_RESULT=\$?
  # Now do the same for detekt
  ./gradlew detekt
  DETEKT_RESULT=\$?
  docker run -it --rm -v "$(pwd):/workdir" ghcr.io/ffurrer2/yamllint:latest .
  YAML_RESULT=\$?
  # unstash the stashed changes
  git stash pop -q
  # return the combined spotless and detekt exit code
  exit \$SPOTLESS_RESULT && \$DETEKT_RESULT && \$YAML_RESULT
  EOF
  chmod +x .git/hooks/commit-msg
  ```

## Server

- No persistence
- No UI
- Websocket API
- Rooms "just" exist as long as there's a user connected
- Room settings (like list of available cards) cannot be changed, but can be
  provided upon room creation
- GET `rooms/new` → Create new random id and redirect to `rooms/{id}`
- GET `rooms/new?{settings}` → Create new random id and redirect to
  `rooms/{id}?{settings}`
- GET `rooms/{id}?settings={settings}` → Create new room if not exists (settings
  ignored if room already exists), establish WS connection
- Available `{settings}` format tbd.
- Default settings tbd.
- Since this is a "game", we cannot allow the players to cheat. All state
  managed by the server. Players can request a change (eg. "I vote 13") and will
  receive either the updated game state in full (no problem since it's tiny), or
  an error message (eg. "Cannot vote now because not in vote phase")
- Powered by quarkus?
- Written in Kotlin, written in java if kotlin with quarkus is too much of a
  headache
- Compiled to native binary, optionally provide a docker image

## Client

- Nice TUI client
- Native executable
- Filename is `pp` for ... planning poker
- Zero required configuration or arguments
- Default server URL tbd.
- Run without args to create a new room: `pp`
- Run with room id to join a room (will be created if not exists):
  `pp my-room-id`, `pp "My convoluted room id"` or `pp "☠️ my danger room 🚨⚠️"`
- Settings can be provided via file `$HOME/.config/pp.ini` (`ini` format,
  obviously)
- Possible settings include

   - Server to use
   - Default room id
   - username
   - Some kind of "usertype", to distinguish "voter" vs "observer"

- Written in golang or rust
- There are some known libs from the golangiverse we could use:

   - [tview](https://github.com/rivo/tview) for TUI
   - [viper](https://github.com/spf13/viper) for configuration

## Meta

- Go wild with commit hooks and linters:

   - Markdown files
   - Code files (eg. `detekt`, `ktlint`, for kotlin. No idea what the
     golang/rust ppl do)
      - Side project: create a native binary exe for detekt, since running jvm
        stuff on every commit is the worst
   - Commit msgs - enforce conventional commits

- Version numbers, release notes auto generated from landed commits.
- Every commit should strive for perfection
- Exceptional documentation,
