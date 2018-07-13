# Run pull-requests and release candidates with Docker
You can now functionally test pull-requests with docker / docker-compose.

## Run only the MOLGENIS-image
You can run only the image with your own local services

```bash
docker run registry.molgenis.org/molgenis/molgenis-app:#tag#
```

### On MacBook
On mac you need to bind the service urls to your own host by adding *docker.for.mac.localhost*. 

```bash
docker run \
    -e db_uri=jdbc:postgresql://docker.for.mac.localhost/molgenis \
    -e elasticsearch.transport.addresses=docker.for.mac.localhost:9300 \
    -e opencpu.uri.host=docker.for.mac.localhost \
    registry.molgenis.org/molgenis/molgenis-app:#tag#
```

## Run the MOLGENIS-image with integrated services
>note: make sure when you kill the docker-compose instance you also run ```docker-compose down``` to cleanup volumes and network layers! 

We support 2 operating systems at the moment.
- Windows 8 (and beyond)
- OSX or MacOS

### For Mac users
With docker-compose:

*Example*

Run specific tag:

```bash
export TAG=PR-7492-3; docker-compose up
```

### For Windows users (from Windows 8 and forward)
In powershell:

*Example*:

Run specific tag:

```bash
$env:TAG="PR-7492-3"; docker-compose up
```

In normal (IntelliJ or Windows `cmd`) prompt:

```bash
SET TAG=PR-7492-3 && docker-compose up
```