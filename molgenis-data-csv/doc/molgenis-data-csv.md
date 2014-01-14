## Introduction

The molgenis-data-csv package is a csv (comma separated values) file backend for Molgenis. It is called molgenis-data-csv but you can specify the separator char. So you can also use it for example tsv (tab separated values). You can use csv files as data backend by registering it but you can also use the package for just reading and writing csv files without registering it as backend.


## Core molgenis-data-csv classes
<img src="molgenis-data-csv.png" />

**The light grey classes are provided by the core molgenis-data module**

### CsvRepository
Repository implementation for a csv file. The name of the file without the extension is considered to be the entityname end the first row is considered to contain the attributenames. All attributes are considered to be of type 'string' because there is no way for the user to define it (yet). 

### CsvEntitySource
EntitySource implementation for csv files. You can use csv, txt, tsv or zip files. Csv and txt files must have a comma as separator, tsv a tab. Zip files are a collection of csv, txt or tsv files. The names of the files are the entity names.

### CsvEntitySourceFactory
EntitySourceFactory implementation for csv. The url prefix for excel is `csv://`. You can create an `EntitySource` using a file or an molgenis data url.

### CsvWriter
With a `CsvWriter` you can write entities to a csv file.


### Usage examlples
***Read entities from a csv file***

```
Repository<Entity> repo = new CsvRepository(new File("/Users/test/test.csv"), null);
try
{
	for (Entity entity : repo)
    {
    	System.out.println(entity);
    }
}
finally
{
 	repo.close();
}

```

***Print all csv filenames (entitynames) in a zip file***

```
EntitySource entitySource = new CsvEntitySourceFactory().create(new File("/Users/test/test.zip"));
try
{
    for (String name : entitySource.getEntityNames())
    {
        System.out.println(name);
    }
}
finally
{
    entitySource.close();
}
```

***Register a csv file as data backend***

```
DataService dataService = ...;
dataService.registerEntitySource("csv:///users/test/test.csv");
```

***Write entities to a csv file***

```
Writable<Entity> writable = new CsvWriter<Entity>(new OutputStreamWriter(new FileOutputStream(file), Arrays.asList("col1", col2));
try
{
	Entity entity = new MapEntity();
	entity.set("col1", "val1");
	entity.set("col2", "val2");
	writable.add(entity);
}
finally
{
	writable.close();
}

```

