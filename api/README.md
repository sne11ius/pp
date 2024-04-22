# pp-api

This is the API part of pp.

## Native build

To build a docker image run the following command. This this make take some
time, but the resulting image should be small (about 40mb).

*Important*: This command must be run in the root of this repo, *not* in the
`api` directory. That's because the build needs the `.git` directory to extract
the git hash of the current commit.

```shell
docker build -f api/src/main/docker/Dockerfile.distroless -t pp/api .
```

## Running the application in dev mode

You can run the application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only
> at [localhost](http://localhost:8080/q/dev/).

## Packaging and running

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the
`build/quarkus-app/lib/` directory.

The application is now runnable using
`java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using
`java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build
in a container using:

```shell script
./gradlew build -Dquarkus.package.type=native \
  -Dquarkus.native.container-build=true
```

You can then execute your native executable with:
`./build/pp-server-1.0.0-SNAPSHOT-runner`

## License

Licensed under the EUPL. See [LICENSE](../LICENSE) file.
