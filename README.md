# pp - Planning Poker in Your Terminal

🎉 Welcome to **pp**! 🎉

[![codecov](https://codecov.io/gh/sne11ius/pp/graph/badge.svg?token=PDADRFW5QB)](https://codecov.io/gh/sne11ius/pp)

## What is pp?

**pp** (Planning Poker) is a TUI (Text User Interface) application designed to
make your estimation sessions efficient. No more distractions from flashy UIs –
focus on what really matters: your team's estimates!

Wowee! Your business overlords surely won't be able to run a TUI program, but
still want to view the game in real time very much. That's why we created a read
only webpage to watch the game:
[https://pp.discordia.network](https://pp.discordia.network)

## Features

- **Terminal-Based:** Runs directly in your terminal, keeping things simple and
  distraction-free.
- **Intuitive TUI:** User-friendly text interface that everyone can use.
- **Collaborative:** Perfect for remote teams or those who love working in the
  terminal.
- **Lightweight:** Zero dependencies native binary.

## Installation

pp is a single native executable and currently available for x86_64 based linux.

### Download via browser

Head over to [Releases](https://github.com/sne11ius/pp/releases) and download
the latest *pp client for linux* binary file. Don't forget to set the executable
flag on the downloaded file.

### Download via shell

The following lines should have you running the latest version in no time

```shell
curl -s https://api.github.com/repos/sne11ius/pp/releases/latest | \
  jq -r '.assets[] | select(.name == "pp") | .browser_download_url' | \
  xargs curl -L -o pp && \
  chmod +x ./pp
```

### From source

If you have [go](https://go.dev/) installed, you can also install directly from
source:

```shell
go install github.com/sne11ius/pp/client@latest
```

## Usage

Fire up your terminal and start a planning poker session:

Running `pp` will pick up your `$USER`, connect to the default server and join
a random room.

If you want to have more control, you can use cli parameters, env variables or a
configuration file.

### Configuration file

You can configure pp by creating a configuration file `pp.config.yaml` in

- `$PWD` or
- `$HOME` or
- `$Home/.config`

This example shows all available entries:

```yaml
# file pp.config.yaml

# set a user name
name: "💖🦋 my-user 🦋💖"
# set a room name
room: "☠️ my danger room 🚨⚠️"
# set a custom server, in case you want to host your own server
server: https://pp.my.gtld
```

### Env / Params

You can also configure settings with env vars or cli parameters.

#### Room

Use env var `ROOM` or parameter `-r` (or `--room`) to join any room:
`ROOM=my_room ./pp` or `./pp -r my_room` or `./pp --room "☠ my danger room 🚨⚠"`


#### Username

Use env var `NAME` or parameter `-n` (or `--name`) to set any username:
`NAME=my_user ./pp` or `./pp -n my_user` or `./pp --name "🤭 😈 😌 🤪 😊 "`

If no username is set, it defaults to `$USER`.

#### Server

Use env var `SERVER` or parameter `-s` (or `--server`) to connect a different
server (default is `https://pp.discordia.network`):
`SERVER=http://localhost:8080 ./pp`

## Screenshots

***Coming soon...***

## Contributing

***Make this a "how to dev" section or link to dedicated doc

We ❤️ contributions! Here's how you can help:

- Fork the repository
- Create a new branch (`git checkout -b feature-foo`)
- Commit your changes (`git commit -am 'feat: Add foo feature'`)
- Push to the branch (`git push origin feature-foo`)
- Create a new Pull Request

## Acknowledgements

Thanks to all our contributors and users who make pp better every day!

Happy Planning! 🚀

Your pp Team

## License

Licensed under the EUPL. See [LICENSE](./LICENSE) file.
