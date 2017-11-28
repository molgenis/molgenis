MOLGENIS Beacon API
===================

**
Create Beacons and Beacon organisations on-top of your genetic data using MOLGENIS Beacons.
**

##### What is a MOLGENIS beacon
A beacon is a gateway to your data. By creating a beacon and pointing at specific data sets within the MOLGENIS database, you allow
people to query multiple data sets via one entry point.

The MOLGENIS Beacons are meant to be used with genetic data sets containing at least the following column types:
* Chromosome
* Start position
* Reference allele(s)
* Alternative allele(s)

A beacon exposes your data sets to the world, allowing for variant discovery. 
A MOLGENIS beacon only exposes whether a specific variant **exists**. Nothing more.

##### What is a MOLGENIS Organization
An organization can be used to organize one or more beacons. An organization is the business card for your beacons.
When hooking up your beacon to the global https://beacon-network.org, the organization information is shown when your beacons respond to queries.

Creating your first beacon
--------------------------
When you have uploaded some genetic data sets in the form of an EMX or VCF, you can go to the Dataexplorer to start creating your first beacon.  
The following examples work with [this data](../data/beacon_set.vcf).

Select the *Beacon* table in the dropdown, and add a new row.

![Creating a Beacon](../images/beacon/create-beacon-form.png?raw=true, "beacon/create-beacon-form")

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
    "name": "My first Beacon",
    "apiVersion": "v0.3.0",
    "description": "The first beacon ever made",
    "version": "v0.0.1",
    "datasets": [
      {
        "id": "beacon_set",
        "name": "beacon_set"
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
  "name": "My first Beacon",
  "apiVersion": "v0.3.0",
  "description": "The first beacon ever made",
  "version": "v0.0.1",
  "datasets": [
    {
      "id": "beacon_set",
      "name": "beacon_set"
    }
  ]
}
```

**Query the beacon for a variant via GET**  
`http://localhost:8080/beacon/MyFirstBeacon/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C`

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

**Query the beacon for a variant via POST**  


**When querying goes wrong**  
When an exception occured, we return a response containing a BeaconError

```json
{
  "beaconId": "MyFirstBeacon",
  "error": {
    "errorCode": 1,
    "message": "Some error occured in EntityType [beacon_set]"
  },
  "alleleRequest": {
    "referenceName": "7",
    "start": 130148888,
    "referenceBases": "A",
    "alternateBases": "C"
  }
}
```

