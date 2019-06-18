**
MOLGENIS has a data api behind which there is great flexibility in the data backends. We use PostGreSQL and ElasticSearch. This section describes how you can create a new back-end, e.g a NoSQL store or a file reader/writer
**

The best way to learn how backends are created are to first study the core interfaces and then use one of the existing backends as template.

# Core interfaces

MOLGENIS currently has the following 'data' interfaces 

* [Entity](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/Entity.java)
* [DataService](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/DataService.java)
* [Repository](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/Repository.java)
* [RepositoryCollection](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/RepositoryCollection.java)
* [Query](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/Query.java)

# Existing backends

MOLGENIS currently has the following backend implementations:

* [PostGreSQL](https://github.com/molgenis/molgenis/tree/master/molgenis-data-postgresql/src/main/java/org/molgenis/data/postgresql)
* [Elastic Search](https://github.com/molgenis/molgenis/tree/master/molgenis-data-elasticsearch/src/main/java/org/molgenis/data/elasticsearch)
* [Microsoft Excel](https://github.com/molgenis/molgenis/tree/master/molgenis-data-excel/src/main/java/org/molgenis/data/excel)
* [Comma Seperated Values](https://github.com/molgenis/molgenis/tree/master/molgenis-data-csv/src/main/java/org/molgenis/data/csv) 
* [Variant Calling Format, VCF](https://github.com/molgenis/molgenis/tree/master/molgenis-data-vcf/src/main/java/org/molgenis/data/vcf)
* [Java Persistency API, JPA](https://github.com/molgenis/molgenis/tree/master/molgenis-data-jpa/src/main/java/org/molgenis/data/jpa)

# How to create a new backend

Minimally you need to implement RepositoryCollection and Repository.

Simple example available is [InMemoryRepository](https://github.com/molgenis/molgenis/tree/master/molgenis-data/src/main/java/org/molgenis/data/mem).

If you want users to choose it when uploading EMX you need to register your backend to MOLGENIS. This is done via Spring framework as specified in:
* [WebAppConfig](https://github.com/molgenis/molgenis/blob/master/molgenis-app/src/main/java/org/molgenis/app/WebAppConfig.java)