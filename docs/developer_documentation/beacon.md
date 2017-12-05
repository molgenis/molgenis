MOLGENIS Beacon API
===================

> Create Beacons and Beacon organisations on-top of your genetic data using MOLGENIS Beacons.

##### What is a MOLGENIS beacon
A beacon is a gateway to your data. By creating a beacon and pointing at specific data sets within the MOLGENIS database, you allow
people to query multiple data sets via one entry point. 

Read more about beacons [here](https://beacon-network.org/#/about).

The MOLGENIS Beacons are meant to be used with genetic data sets containing at least the following column types:
* Chromosome
* Start position
* Reference allele(s)
* Alternative allele(s)

A MOLGENIS beacon only exposes whether a specific variant **exists**. Nothing more.

##### What is a MOLGENIS Organization
An organization can be used to organize one or more beacons. An organization is the business card for your beacons.
When hooking up your beacon to the global https://beacon-network.org, the organization information is shown when your beacons respond to queries.

Creating your first beacon
--------------------------
When you have uploaded some genetic data sets in the form of an EMX or VCF, you can go to the Dataexplorer to start creating your first beacon.  

The following examples work with [this data](../data/beacon_set.vcf).

Select the *Beacon* table in the dropdown, and add a new row.

![Creating a Beacon](../images/beacon/add-beacon-form.png?raw=true, "beacon/add-beacon-form")

The beacon created here has only one data set, namely *beacon_set*.  
Note that the Organization is still empty, we will come back to that later.

Now that we have created a beacon, we can actually already query for variants.

**Seeing a list of all the beacons**  
`http://localhost:8080/beacon/list`   

*produces*  
```json
[
 {
   "id": "MyFirstBeacon",
   "name": "My First Beacon",
   "apiVersion": "v0.3.0",
   "datasets": [
     {
       "id": "beacondataset",
       "name": "My First Beacon dataset"
     }
   ]
 }
]
```

**See the info of one beacon**  
`http://localhost:8080/beacon/MyFirstBeacon`

*produces*  
```json
{
   "id": "MyFirstBeacon",
   "name": "My First Beacon",
   "apiVersion": "v0.3.0",
   "datasets": [
     {
       "id": "beacondataset",
       "name": "My First Beacon dataset"
     }
   ]
}
```

**Query the beacon for a variant via GET or POST**  
`http://localhost:8080/beacon/MyFirstBeacon/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C`

`http://localhost:8080/beacon/MyFirstBeacon/query`  
```json
{
  "referenceName": "7",
  "start": 130148888,
  "referenceBases": "A",
  "alternateBases": "C"
}
```

*produces*
```json
{
  "beaconId": "MyFirstBeacon",
  "exists": true,
  "alleleRequest": {
    "referenceName": "7",
    "start": 130148888,
    "referenceBases": "A",
    "alternateBases": "C"
  }
}
```

**When querying goes wrong**  
When an exception occurs, we return a response containing a BeaconError

```json
{
  "beaconId": "MyFirstBeacon",
  "error": {
    "errorCode": 400,
    "message": "Unknown beacon [MyFirstBeacon]"
  },
  "alleleRequest": {
    "referenceName": "7",
    "start": 130148888,
    "referenceBases": "C",
    "alternateBases": "A"
  }
}
```

Creating an organization
------------------------

For your beacon to look nice to the world, we will add it to an organization.

In the dataexplorer, go to the dropdown in the top right, select the *BeaconOrganization* table, and add a new row

![Creating a Beacon Organization](../images/beacon/create-organization-form.png?raw=true, "beacon/create-organization-form")

Configure dataset as Beacon

![Configure dataset as a beacon](../images/beacon/configure-beacon-as-dataset-form.png?raw=true, "beacon/configure-beacon-as-dataset-form")

And now we can link to this organization and dataset by editing the Beacon row we created before

![Adding a Beacon](../images/beacon/add-beacon-form.png?raw=true, "beacon/add-beacon-form")
 
And by requesting info on our beacon again  
`http://localhost:8080/beacon/MyFirstBeacon`

We now get information on our organization

```json
{
  "id": "MyFirstBeacon",
  "name": "My First Beacon",
  "apiVersion": "v0.3.0",
  "organization": {
    "id": "beaconorg",
    "name": "Beacon organization",
    "description": "Beacon organizational unit"
  },
  "datasets": [
    {
      "id": "beacondataset",
      "name": "My First Beacon dataset"
    }
  ]
}
```

Specifications
--------------
The complete API schema and methods can be found [on this GitHub page](https://github.com/ga4gh/beacon-team/tree/develop/src/main/resources/avro)

Within MOLGENIS we decided on a subset of the API. Due to the dynamic nature of our data, we can not always supply all fields.