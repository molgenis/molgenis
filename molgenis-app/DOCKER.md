# Run Docker
You can run Docker with 


## On MacBook

When build locally with 

```bash
docker run \
    -e db_uri=jdbc:postgresql://docker.for.mac.localhost/molgenis \
    -e elasticsearch.transport.addresses=docker.for.mac.localhost:9300 \
    -e opencpu.uri.host=docker.for.mac.localhost \
    #containerId#
```

With docker-compose:

```bash
docker-compose run -e TAG=#pr-tag or branch-tag# molgenis
```