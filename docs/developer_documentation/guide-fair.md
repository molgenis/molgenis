##FAIR data endpoint
MOLGENIS partly supports the FAIR(Findable, Accessible, Interoperable, and Re-usable) [data principles](https://www.force11.org/group/fairgroup/fairprinciples).

The actual data used in the responses of the FAIR data endpoint are specified in the entities in the "fdp" package. The entities are named the same as the endpoints shown below. (Metadata, Catalog, Dataset, Distribution)

The examples in the documentation below work on servers where this [demodata](https://github.com/bartcharbon/molgenis/raw/32f760792c13d48ba05e2be7ba1a9fefb53d175f/molgenis-fair/src/test/resources/FDP.xlsx) is loaded

The currently (molgenis 3.0.0 and higher) supported endpoints are:
###Metadata (/fdp/)

Example: ```http://molgenis.mydomain.example/fdp/ ```
This endpoint returns information the following information about the server:
identifier,title,issued,modified,hasVersion,license,description,APIVersion,publisher,contact,language,rights,contains

For a description of those items check [here](https://dtl-fair.atlassian.net/wiki/display/FDP/FAIR+Data+Point+Software+Specification)

###Catalog (/fdp/[catalogID]) 

Example: ```http://molgenis.mydomain.example/fdp/catalogue/```
This endpoint returns information the following information about the datasets available on this server:
title,identifier,issued,modified,hasVersion,publisher,description,language,license,rights,homepage,dataset,themeTaxonomy

For a description of those items check [here](http://www.w3.org/TR/vocab-dcat/#Class:_Catalog)

###Dataset (/fdp/[catalogID]/[datasetID]/)

Example: ```http://molgenis.mydomain.example/fdp/catalogue/biobanks```
This endpoint returns information the following information about the dataset:
title,identifier,issued,modified,publisher,hasVersion,description,language,license,rights,distribution,theme,contactPoint,keyword,landingPage

For a description of those items check [here](http://www.w3.org/TR/vocab-dcat/#Class:_Dataset)

###Distribution(/fdp/[catalogID]}/[datasetID]/[distributionID])

Example: ```http://molgenis.mydomain.example/fdp/catalogue/biobanks/distribution```
This endpoint returns information the following information about the distribution of the dataset:
title,identifier,issued,modified,license,hasVersion,rights,description,accessURL,downloadURL,mediaType,format,byteSize

For a description of those items check [here](http://www.w3.org/TR/vocab-dcat/#Class:_Distribution)

###Response
All the endpoints respond in [turtle format](http://www.w3.org/TR/turtle/)
