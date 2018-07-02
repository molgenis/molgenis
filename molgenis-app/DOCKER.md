# Run pull-requests and release candidates with Docker
You can now functionally test pull-requests with docker / docker-compose.

## Run only the MOLGENIS-image
You can run only the image with your own local services

```bash
docker run registry.molgenis.org/molgenis/molgenis-app:#tag#
```

### On MacBook
On mac you need to bind the service urls to you own host by adding *docker.for.mac.localhost*. 

```bash
docker run \
    -e db_uri=jdbc:postgresql://docker.for.mac.localhost/molgenis \
    -e elasticsearch.transport.addresses=docker.for.mac.localhost:9300 \
    -e opencpu.uri.host=docker.for.mac.localhost \
    #containerId#
```

## Run the MOLGENIS-image with integrated services
With docker-compose:

Run specific tag:

```bash
export TAG=#pr-tag or branch-tag#
docker-compose up
```