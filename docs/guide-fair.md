# FAIR Data Point

MOLGENIS partly supports the FAIR(Findable, Accessible, Interoperable, and Re-usable)
[principles](https://www.force11.org/group/fairgroup/fairprinciples).

## Model
The actual data used in the responses of the FAIR data endpoint
are specified in the entities in the "fdp" model package defined in
[FDP.xlsx](https://github.com/molgenis/molgenis/blob/master/molgenis-api-fair/src/main/resources/FDP.xlsx).
This model emx contains an implementation of the https://github.com/FAIRDataTeam/FAIRDataPoint-Spec/.
The entities and attributes in the model are tagged using ontology terms
to generate RDF statements based on the values in the rows. 

## Test data
To test it out you can import the demo data in
[FDP_test_data.xlsx](https://github.com/molgenis/molgenis/blob/master/molgenis-api-fair/src/test/resources/FDP_test_data.xlsx).

## Endpoints

### Metadata entrypoint: `/api/fdp/`
Example: `http://molgenis.mydomain.example/api/fdp/`
This endpoint transforms the first row of the `fdp_Metadata` entity type to RDF statements.

### Any DCAT Resource: `/api/fdp/{type}/{id}`
Example: `http://molgenis.mydomain.example/api/fdp/fdp_Catalog/catalogue`.
This endpoint transforms any row of any entity type to RDF statements,
as long as the entity type is tagged as a
`http://www.w3.org/ns/dcat#Resource`. 

### Preview
You can preview the RDF generated for `fdp_Metadata`, or an `fdp_Catalog`, `fdp_Dataset`
or `fdp_Distribution` in the Data Explorer by clicking view-row button.

### Ping home
You can "Ping home" to add your FAIR Data Point to the `https://home.fairdatapoint.org` index by
clicking the ping button in the `fdp_Metadata` preview.

### Format
All the endpoints respond in [turtle format](http://www.w3.org/TR/turtle/)