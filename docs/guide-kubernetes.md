# Kubernetes (experimental)
> note: We are currently moving MOLGENIS to the cloud via Kubernetes. However, **this is NOT production ready**.

There are a few basic concepts you should know before you read this guide.
- [Kubernetes](https://kubernetes.io/docs/home/)
- [Helm](https://helm.sh/)
- [Rancher](https://rancher.com/what-is-rancher/overview/)

## Deployment
The architecture of MOLGENIS to run in Kubernetes is displayed in the following figure.

![MOLGENIS components](images/install/molgenis_architecture_kubernetes.svg?raw=true)

### Helm
We use helm to deploy the MOLGENIS instance. We expose our charts on https://helm.molgenis.org. You can add it as your own helm repository and install it by executing the following command:

```bash
helm repo add molgenis https://helm.molgenis.org
helm repo update
helm install molgenis
```

### Rancher
For our own orchestration, we use Rancher. We support Rancher catalogs. Just like with helm you can add the catalog by adding the helm repo to your Rancher instance.

Our Rancher catalogs can be found here: https://helm.molgenis.org.

You can use the [rancher-cli](https://rancher.com/docs/rancher/v2.x/en/cli/) to deploy the app on Rancher:

```rancher app install molgenis```

### Documentation
For MOLGENIS chart documentation you can navigate [here](https://github.com/molgenis/molgenis-ops-helm/tree/master/charts/molgenis).

> note: You can find detailed documentation on the helm charts we expose [here](https://github.com/molgenis/molgenis-ops-helm). 

