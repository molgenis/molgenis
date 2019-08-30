# Developing in IntelliJ with Docker
We have several backend services which we run to debug and test the MOLGENIS backend webapp.
* [OpenJDK 11](https://adoptopenjdk.net/)
* [Apache Tomcat v9.0.x](http://tomcat.apache.org/) 
* [PostgreSQL v11.1](https://www.postgresql.org/)
* [Elasticsearch v5.5](https://www.elastic.co/)	
* [Minio v6](https://minio.io/)	
* Optional: [OpenCPU 2.1](https://www.opencpu.org) and [R 3.5.x](https://www.r-project.org/) (enables R scripting feature)	
* Optional: [Python 3.6](https://www.python.org) (enables Python scripting feature)

> **IMPORTANT:** to switch from local services installed with executables to this docker deployment you need to turn of the local services to make the ports available again to your host.

## Deploy
You first need to configure the specifics for your OS.

**For Mac**
Install Docker: https://hub.docker.com/editions/community/docker-ce-desktop-mac

**For Windows**
Install Docker: https://hub.docker.com/editions/community/docker-ce-desktop-windows

> **IMPORTANT**: Go to Windows Docker Desktop App and check ```Expose daemon on tcp://localhost:2375 without TLS``` to expose the docker deamon to your localhost 

### Run
You can create it all up by right-clicking the [docker-compose file](https://github.com/molgenis/molgenis/blob/master/molgenis-app/development/docker-compose.yml) and click on **Create 'dev-env: Compose...'**.

> **Please be advised**: when you use the build configuration in IntelliJ, please check the option ```--build, force build images```.

You need to add the following environments variables to the *Run configuration*:

**For Windows**
```env
BACKEND=./backend-for-windows.conf
FRONTEND=molgenis/molgenis-frontend:latest
```

**For Mac**
```env
BACKEND=./backend-for-mac.conf
FRONTEND=molgenis/molgenis-frontend:latest
```

Alternatively you can run the stack by executing on the cli:

**For Windows**

```batch
molgenis-app/dev-env/docker-stack.bat start
``` 

**For Mac**

```bash
molgenis-app/dev-env/docker-stack.bash start
``` 

Test your work at: <http://localhost>.

### Alternative configuration for testing purposes
When you want to test integration with the frontend you can specify another frontend image by overriding ```FRONTEND``` in the environment variables.
 
**For IntelliJ** 
In the *Run configuration* override.

```env
FRONTEND=registry.molgenis.org/molgenis/molgenis-frontend:PR-1-1
```

> For tags please check: [pull requests for frontend](https://registry.molgenis.org/#browse/browse:docker:v2/molgenis/molgenis-frontend/tags)

**For BATCH / BASH startup**

Override:

*For Windows*
```batch
@ECHO OFF
...
SET FRONTEND=registry.molgenis.org/molgenis/molgenis-frontend:PR-1-1
...
```

*For Mac*
```bash
#!/bin/bash
...
export FRONTEND=registry.molgenis.org/molgenis/molgenis-frontend:PR-1-1
...
```
> **Minio**: when you run Minio in the docker-compose stack it will bind to the localhost on your user-dir/minio. Be sure that the minio directory is present in your user directory.
> **NGINX**: you can see that the port number is not there. NGINX is resolving the frontend and will always be served on port 80. 

To debug:

```bash/batch
# show running containers
docker ps
# show logging of specific container
docker logs #container id#1`
# exec into a running container
docker exec -it #container id# bash
```

## Teardown
Dont forget to stop the running services after use.

**For Windows on cli**
```batch
docker-stack.bat shutdown
```

**For Mac on cli**
```bash
docker-stack.bash shutdown
```

>note: also when you exited with ```CTRL+C```.

**In IntelliJ**

Right-click within the Docker-service tab on *Compose: dev-env* and click on *Down*.

### When all else fails
When you are not sure that everything went the way it should be, these commands will purge everything on your system:

**For Windows on cli**
```batch
docker-stack.bat terminate
```

**For Mac on cli**
```bash
docker-stack.bash terminate
```

## Accessing services from host machine
There are a few clients you can use to access the docker services from your local machine.

### Postgres
We use ```psql``` to access postgres and do database changes.

```bash/batch
psql -h localhost -p 5432 -U molgenis -W
``` 

> **IMPORTANT:** In Docker the postgres user is non-existent. You have 1 superuser which is defined in the docker-compose (username: molgenis, password: molgenis).
>  
> There is only 1 scheme as well. This won't allow you to drop the database on the container. 
>
> The right way to do this is to shutdown the services and to purge the PostgreSQL docker, image and volume and then restart the services again. 
