# MOLGENIS-SEARCH Documentation

## Overview
The search module adds indexing and search functionality to your molgenis app. You can index anything that implements the `org.molgenis.framework.tupletable.TupleTable` interface. The 
The module consists of a SearchService interface and a JSON http endpoint.

The molgenis-search-elasticsearch module provides an elasticsearch implementation of the SearchService interface. The search modules do not depend on a specific datamodel like omx but can be used with every datamodel.

## Adding search to your molgenis app
1. Add the molgenis-search module and the molgenis-search-elasticsearch modules as a dependency in your pom.xml:

       <dependency>
         	<groupId>org.molgenis</groupId>
         	<artifactId>molgenis-search</artifactId>
         	<version>0.0.1-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.molgenis</groupId>
			<artifactId>molgenis-search-elasticsearch</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
2. Import the `org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig` spring configuration class into your spring configuration. If you want to secure the api you should import the `org.molgenis.search.SearchSecurityConfig` config class. Users then need to be logged in to be able to use the search api.  
Example:

        @Configuration
        @EnableWebMvc
        @ComponentScan("org.molgenis")
        @Import({ EmbeddedElasticSearchConfig.class, SearchSecurityConfig.class })
        public class WebAppConfig extends WebMvcConfigurerAdapter
        {
        } 
    

## Indexing
Before you can query you need to index your data. This can be done bny calling this method of the SearchService interface: 
    
    void indexTupleTable(String documentType, TupleTable tupleTable);
    
Where documentType is a unique name of the data type you are indexing.

A common case is that you need to index all `org.molgenis.omx.observ.ObservationSet` values of a  `org.molgenis.omx.observ.DataSet`. This can be done with the `org.molgenis.omx.dataset.DataSetTable`:

    DataSet dataSet = …
    searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, database));
    
If you are using the molgenis-omx-dataexplorer package new DataSets are automatically indexed at startup and every DataSet if reindexed at night. Realtime indexing is not yet implemented. There is also a DataSetsIndexerPlugin available to index datasets manually.

## Querying
The search endpoint is deployed under the `/search` uri. So if you have installed your app on localhost and port 8080 your search endpoint url is `http://localhost:8080/search`. Search requests are done by posting JSON (see [www.json.org](http://www.json.org)) messages. 

###SearchRequest
 A request consist of a documentType (in case of a DataSet this is the DataSet name), a list of queryrules and a list of fields to return. (see `org.molgenis.search.SearchRequest`).    

**SearchRequest:**

* documentType: string, optional. If not provided a search over all document types will be performed
* queryRules: list of QueryRule, optional. If not provided returns all documents
* fieldsToReturn: list of string, optional. If not provided returns all fields  
 <br />
	
Example:

       {
    	    "documentType":"Carcinoma",
    	    "queryRules":[{"operator":"LIMIT","value":1}],
    	    "fieldsToReturn":["Carcinoma_Breast_Carcinoma", "Individual"]
        }
        
     
###SearchResult
The response consist of the total nr of hits, and the searchhits (standard the first 10 hits are returned, can be changed by the LIMIT and OFFSET queryrules). A searchhit contains the document id, the document type and a columnValueMap wich contains all requested fields with there values. (see `org.molgenis.search.SearchResult`). If there was an error it contains an errorMessage.    
<br />

**SearchResult:**

* totalHitCount: long
* searchHits: list of Hit
* errorMessage: string, only in case of an error
<br />
<br />
	
**Hit:**

* id: string, document id
* documentType: string
* columnValueMap: map with key:string, fieldname and value:object fieldvalue
<br />
<br />
	
Example:
	
        {
           "totalHitCount": 1600,
           "searchHits":
           [
               {
                   "id": "XxDjPNODSZGGz5fGfOkRrw",
                   "documentType": "Carcinoma",
                   "columnValueMap":
                   {
                       "Individual": "id_1",
                       "Carcinoma_Breast_Carcinoma": "No"
                   }
               }
           ]
        }

###QueryRule
A queryrule contains an operator, a field and a value (see `org.molgenis.framework.db.QueryRule`)
<br />
<br />

**QueryRule:**

* operator: enum (string). supported values: [AND, OR, NOT, EQUALS, LIKE, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, SEARCH, SORTASC, SORTDESC, LIMIT, OFFSET]
* field: string, optional depends on operator
* value: object, optional depends on operator
<br />
<br />

Examples:

     {"operator":"EQUALS", "field":"Carcinoma_Cervical_Carcinoma","value":"Yes"}
     {"operator": "GREATER", "field":"Carcinoma_Age", "value":50}
     
     
You can combine queryrules in a request:

	{
    	"queryRules":
    	[
    		{"operator":"EQUALS", "field":"Carcinoma_Cervical_Carcinoma","value":"Yes"}, 
    		{"operator":"AND"}, 
    		{"operator": "GREATER", "field":"Carcinoma_Age", "value":50}
    	]
    }
    
For free text search use SEARCH:

    {"operator":"SEARCH", "value":"Blood"}
    	
Sorting (in this case the value contains the field to sort on):

	{"operator":"SORTDESC", "value":"Carcinoma_Body_Weight"}
	
You can page through the result hits with LIMIT and OFFSET:

	{
		"queryRules":
		[
			{"operator":"OFFSET", "value":20}, 
			{"operator":"LIMIT", "value":20}
		]
	} 


        

