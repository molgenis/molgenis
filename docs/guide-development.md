# Developing MOLGENIS
To develop MOLGENIS on you machine we recommend to use IntelliJ as IDE. 
You need to install the prerequisites as well.

## Prerequisites for MOLGENIS development
The components needed to run MOLGENIS locally are:

![MOLGENIS components](images/install/molgenis_architecture.svg?raw=true)

>note: You can download, install and use MOLGENIS for free under license [LGPLv3](https://www.gnu.org/licenses/lgpl-3.0.en.html).

You can spin up a fully configured backend with [docker-compose](https://github.com/molgenis/molgenis/blob/master/molgenis-app/development/docker-compose.yml). You can obtain it here: [install docker-compose](https://docs.docker.com/compose/install/).  

You need to install Docker.
* For mac users: https://hub.docker.com/editions/community/docker-ce-desktop-mac 
* For windows users: https://hub.docker.com/editions/community/docker-ce-desktop-windows

Check: [develop locally](../molgenis-app/development/DOCKER.md)

## Get the code
Create account on github.com. 

Copy the clone URL.

```bash
mkdir -p ~/git
cd ~/git 
git clone http://github.com/molgenis/molgenis
``` 

*Optionally select stable molgenis version:*

```bash
git fetch --tags origin
git checkout <tag name: see https://github.com/molgenis/molgenis/releases>
```

## Start MOLGENIS
We use [IntelliJ](guide-using-an-ide-for-backend.md) as IDE to run the code in.