# pp - Planning Poker

Simple client/server planning poker

## Development

### Prerequisites

- [Conform](https://github.com/siderolabs/conform) is required
- Install commit hook with
  ```bash
  cat <<EOF | tee .git/hooks/commit-msg
  #!/bin/sh
  
  # This assumes, conform is in your $PATH
  conform enforce --commit-msg-file \$1
  EOF
  chmod +x .git/hooks/commit-msg
  ```

## Server

- No persistence
- No UI
- Websocket API
- Rooms "just" exist as long as there's a user connected
- Room settings (like list of available cards) cannot be changed, but can be provided upon room creation 
- GET `rooms/new` → Create new random id and redirect to `rooms/{id}`
- GET `rooms/new?{settings}` → Create new random id and redirect to `rooms/{id}?{settings}`
- GET `rooms/{id}?settings={settings}` → Create new room if not exists (settings ignored if room already exists),
  establish WS connection
- Available `{settings}` format tbd.
- Default settings tbd.
- Since this is a "game", we cannot allow the players to cheat. All state managed by the server. Players can request a
  change (eg. "I vote 13") and will receive either the updated game state in full (no problem since it's tiny), or an
  error message (eg. "Cannot vote now because not in vote phase")
- Powered by quarkus?
- Written in Kotlin, written in java if kotlin with quarkus is too much of a headache
- Compiled to native binary, optionally provide a docker image

## Client

- Nice TUI client
- Native executable
- Filename is `pp` for ... planning poker
- Zero required configuration or arguments
- Default server URL tbd.
- Run without args to create a new room: `pp`
- Run with room id to join a room (will be created if not exists): `pp my-room-id`, `pp "My convoluted room id"` or
  `pp "☠️ my danger room 🚨⚠️"`
- Settings can be provided via file `$HOME/.config/pp.ini` (`ini` format, obviously)
- Possible settings include
  - Server to use
  - Default room id
  - username
  - Some kind of "usertype", to distinguish "voter" vs "observer"
- Written in golang or rust
- There are some known libs from the golangiverse we could use:
  - https://github.com/rivo/tview for TUI
  - https://github.com/spf13/viper for configuration

## Meta

- Go wild with commit hooks and linters:
  - Markdown files
  - Code files (eg. `detekt` for kotlin. No idea what the golang/rust ppl do)
    - Side project: create a native binary exe for detekt, since running jvm stuff on every commit is the worst 
  - Commit msgs - enforce conventional commits
- Version numbers, release notes auto generated from landed commits.
- Every commit should strive for perfection
- Exceptional documentation,
