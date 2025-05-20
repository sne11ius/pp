# pp - Planning Poker Server

🎉 Welcome to **pp**! 🎉

[![codecov](https://codecov.io/gh/sne11ius/pp/graph/badge.svg?token=PDADRFW5QB)](https://codecov.io/gh/sne11ius/pp)

## What is pp?

**pp** (Planning Poker) is server application that provides a (mostly
websockets) API to play planning poker. If you don't know what planning poker
is, you might want to read up
[wikipedia](https://en.wikipedia.org/wiki/Planning_poker).

## Clients

If you want to play planning poker using this server, you will need a client.
There are two awesome alternatives right now:

- [ja-ko/ppoker](https://github.com/ja-ko/ppoker) is an opinionated TUI
  (terminal user interface) application, relying heavily on keyboard shortcuts.
  You should totally check it out.
- [pp-client.el](https://github.com/hennes-maertins/pp-client.el) allows you to
  play planning poker inside the best of all operating system: emacs. You should
  totally check it out.

Historically, there was a an "official" client, written in
[golang](https://go.dev/). It was discontinued due the other alternatives being
superior. You can still check out the source from the last commit before removal
at this [hash](https://github.com/sne11ius/pp/commit/f77bbb677e1b2a35d426337769ff18ba9440d49e).

## Running pp

pp is distributed as docker image. A simple

```shell
    docker run ghcr.io/sne11ius/pp-api:latest
```

should get you going after downloading the 40MB image. You can look up specific
versions of pp in the [github container registry](https://github.com/sne11ius/pp/pkgs/container/pp-api).

## Configuration

pp cannot be configured and has no runtime dependencies (eg. no database). pp
runs on port `8080`.

## Development

pp is written using [kotlin](https://kotlinlang.org/) and
[quarkus](https://quarkus.io/).

We use [gradle](https://gradle.org/), [graalvm](https://www.graalvm.org/) and
[upx](https://upx.github.io/) to produce a docker image that only clocks in at
around 40MB.


### Prerequisites

commits/pushes are checked by github actions to verify every change conforms to
our development guidelines. To make sure your changes can be merged and land in
the next release, consider running the checks on each commit.

An example commit hook can be found in [commit-hook.sh](commit-hook.sh). The script
uses [docker](https://www.docker.com/) and [gradle](https://gradle.org/). But
since these tools are also required to build the software itself, this should be
fine.

Use the following script to install a git hook that checks your changes on each
commit.

  ```bash
  rm -f .git/hooks/commit-msg
  cp ./commit-hook.sh .git/hooks/commit-msg
  chmod +x .git/hooks/commit-msg
  ```


### Running in development mode

You can run the application in dev mode that enables live reload using:

```shell script
./gradlew quarkusDev
```

> **NOTE:**  Quarkus ships with a Dev UI, which is available in dev mode only
> at [localhost](http://localhost:8080/q/dev/).

### Release

To build a releasable docker image, run the following command. This may take
quite some time, but the resulting image should be small (about 40MB).

*Important*: This command must be run in the root of this repo, since the build
needs the `.git` directory to extract the git hash of the current commit.

```shell
docker build -f src/main/docker/Dockerfile.distroless -t pp/api .
```


## Contributing

We ❤️️ contributions! Here's how you can help:

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
