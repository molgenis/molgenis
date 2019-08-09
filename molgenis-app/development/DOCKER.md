# Developing in IntelliJ with Docker
We have several backend services which we run to debug and test the MOLGENIS backend webapp.
- postgres
- minio
- elasticsearch
- opencpu
- frontend of MOLGENIS

## Deploy
For mac, override the ```BACKEND``` property in the ```.env``` file with the following content:
```bash
BACKEND=./backend-for-mac.conf
```

For windows, override the ```BACKEND``` property in the ```.env``` file with the following content:
```bash
BACKEND=./backend-for-windows.conf
```

Run the stack by executing:

```bash
docker-compose up (-d when you want to run deamon mode)
```

Alternatively you can right-click the file in IntelliJ

When you want to test integration with the frontend you can specify another frontend image by overriding ```FRONTEND``` in the ```.env``` file.

```bash
FRONTEND=registry.molgenis.org/molgenis/molgenis-frontend:PR-1-1
```

For tags please check: [pull requests for frontend](https://registry.molgenis.org/#browse/browse:docker:v2/molgenis/molgenis-frontend/tags)

## Testing
Test your work at: <http://localhost>.

> note: you can see that the port number is not there. NGINX is resolving the frontend and will always be served on port 80. 

To debug:

```bash
# show running containers
docker ps
# show logging of specific container
docker logs #container id#
# exec into a running container
docker exec -it #container id# bash
```

## Teardown
Dont forget to stop the running services after use.

```bash
docker-compose down
```

>note: also when you exited with ```CTRL+C```.

When you are not sure that everything went the way it should be, these commands will purge everything on your system:

```bash
# delete volumes (not in use)
docker volume prune
# delete containers (not in use)
docker container prune
# delete image (not in use)
docker image prune
```

## Accessing services from host machine
There are a few clients you can use to access the docker services from your local machine.

### Postgres
We use ```psql``` to access postgres and do database changes.

```bash
psql -h localhost -p 5432 -U molgenis -W
``` 

> note: **IMPORTANT:** In Docker the postgres user is non-existent. You have 1 superuser which is defined in the docker-compose (username: molgenis, password: molgenis).
>  
> There is only 1 scheme as well. This won't allow you to drop the database on the container. 
>
> The right way to do this is to shutdown the services and to purge the PostgreSQL docker, image and volume and then restart the services again. 
