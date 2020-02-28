# Molgenis development using Docker

## Prerequisites

A functional Docker and Docker-compose setup is needed. Checkout the
documentation for [Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows),
[MacOS](https://hub.docker.com/editions/community/docker-ce-desktop-mac) or your
Linux distribution on how to setup Docker and Docker-compose.

## Services

Molgenis depends on the following services:

* [OpenJDK 11](https://adoptopenjdk.net/)
* [Apache Tomcat v9.0.x](http://tomcat.apache.org/)
* [PostgreSQL v11.1](https://www.postgresql.org/)
* [Elasticsearch v5.5](https://www.elastic.co/)
* [Minio v6](https://minio.io/)

These services are optional:

* [OpenCPU 2.1](https://www.opencpu.org) and [R 3.5.x](https://www.r-project.org/) (R scripting feature)
* [Python 3.6](https://www.python.org) (Python scripting feature)

## Environment Variables

You can either create a `.env` file to use docker-compose from the
commandline, or add variables to your system's environment. Read the
[Docker documentation](https://docs.docker.com/compose/environment-variables/)
for more information about environment variables.

```bash
cd molgenis-app/dev-env
cp .env.example .env
docker-compose start frontend
# MOLGENIS_HOST must be the Gateway address (e.g. 172.17.0.1)
# when using 'bridge' network mode.
docker inspect frontend
# On Linux, you can find the right property like this:
docker inspect -f '{{range .NetworkSettings.Networks}}{{.Gateway}}{{end}}' frontend
# Stop the service(s)
docker-compose stop
```

* Verify environment variables

```bash
vim .env
# (!) 127.0.0.1 when using host mode
MOLGENIS_HOST=172.17.0.1
MOLGENIS_FRONTEND=molgenis/molgenis-frontend:latest
# On Window & MacOS, bridge mode is mandatory.
# On Linux, host mode is preferred.
NETWORK_MODE=bridge
:wq
docker-compose up
# Open a browser on http://localhost
```

## IntelliJ & Docker

* Create a new run-configuration by right-clicking the [docker-compose file](https://github.com/molgenis/molgenis/blob/master/molgenis-app/dev-env/docker-compose.yml) and selecting *Create 'dev-env: Compose Deployment'*.
* Check the option `--build, force build images`
* Copy the environment variables from `molgenis-app/dev-env/.env.example` to the system clipboard
* Click on the *Browse* icon in the Environment variables input field
* Click the *Paste* button and verify the [variable configuration](#variables-configuration)

## Data Persistence

The ```psql``` client is used to connect to the PostgreSQL database:

```bash
psql -h localhost -p 5432 -U molgenis -W
```

Data for PostgreSQL & ElasticSearch is stored in Docker volumes. Cleaning
persistent data can be done by removing the containers and afterwards
the volumes:

```bash
docker-compose stop
docker-compose rm

# Remove PostgreSQL data volume
docker volume rm dev-env_db-data
# Remove ElasticSearch data volume
docker volume rm dev-env_es-data
```

## Frontend versioning

Testing different frontend versions can be done using another frontend
Docker image. Override the `MOLGENIS_FRONTEND` [variable](#variables-configuration):

```bash
MOLGENIS_FRONTEND=registry.molgenis.org/molgenis/molgenis-frontend:PR-1-1
```

Check frontend [pull requests](https://registry.molgenis.org/#browse/browse:docker:v2/molgenis/molgenis-frontend/tags) for valid tags.

## Troubleshooting

### Windows

* Go to Windows Docker Desktop App and check if this option is checked:
`Expose daemon on tcp://localhost:2375 without TLS` to expose the docker
daemon to your localhost

### Minio

* Be sure that the Minio directory is present in the user directory when running
Minio in the docker-compose stack.
