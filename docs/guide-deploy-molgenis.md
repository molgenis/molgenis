# Deploy MOLGENIS
> when you have questions please email us at: [molgenis-operations@umcg.nl](mailto:molgenis-operations@umcg.nl?subject=[Deployment])

We support three ways in which you can deploy MOLGENIS.                

## Run in development or test
When you are developing on top of MOLGENIS or testing the platform we recommend using [docker-compose](https://github.com/molgenis/docker).

## Run in production
When you run MOLGENIS in production we recommend using the [ansible galaxy deployment](https://galaxy.ansible.com/molgenis).

## Run MOLGENIS on Kubernetes (experimental)
You can run MOLGENIS in kubernetes using the Helm repository we provide:

`helm repo add molgenis https://helm.molgenis.org`

To install a MOLGENIS on your cluster:

`helm install molgenis molgenis` 

## Migration guide
If you are migrating from MOLGENIS x.x.x to x.x.x please read the [migration guide](./guide-deploy-migration.md).
