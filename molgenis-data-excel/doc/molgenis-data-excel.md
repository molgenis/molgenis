## Introduction

The molgenis-data-excel package is an excel data backend for Molgenis. It is an excel implementation of the data api interfaces. It also provides functionality for just reading and writing excel files without the need to register excel as data backend.

## Core molgenis-data-excel classes
<img src="molgenis-data-excel.png" />

**The light grey classes are provided by the core molgenis-data module**

### ExcelEntity
Entity implementation for excel. It represents a row in an excelsheet. 

### ExcelRepository
Repository implementation for excel. It represents an excelsheet and acts like an excelsheet reader. The cells in the first row of the excelsheet are considered to contain the attribute names. All the attributes are defined as type 'string' in the metadata because there is no way for the user to define it (yet). The name of the repository is the name of the excel sheet.

### ExcelEntitySource
EntitySource implementation for excel. It represents an excel file (workbook). You can instantiate it directly or you can use the `ExcelEntitySourceFactory` to create it. Files with the 'xls' or 'xlsx' extension are considered excel files. 

Use `Iterable<String> getEntityNames();` to get the names of the excel sheets in the workbook. To get a specific sheet (repository) you can use `Repository<? extends Entity> getRepositoryByEntityName(String name);` 

The url for registering an `ExcelEntitySource` (excel file) as data backend must be of the form `excel://path_to_excel_file`.


### ExcelEntitySourceFactory
EntitySourceFactory implementation for excel. The url prefix for excel is `excel://`. You can create an `EntitySource` using a file or an molgenis data url.


### Usage examples
**Print all sheetnames of an excel file**

```
EntitySource entitySource = new ExcelEntitySource(new File("/users/test/test.xls"));
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

**Print all rows (entities) of an excel sheet**

```
EntitySource entitySource = new ExcelEntitySource(new File("/users/test/test.xls"));
try
{
	Repository<? extends Entity> repo = entitySource.getRepositoryByEntityName("Sheetname");
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
}
finally
{
	entitySource.close();
}
```

**Register an excel file as data backend**

```
DataService dataService = ...;
dataService.registerEntitySource("excel:///users/test/test.xls");
```

**Create a new excelfile, create a sheet and write entities (rows) to it**

```
WritableFactory<Entity> writableFactory = new ExcelWriter<Entity>(new File("/users/test/test.xls"));
try
{
	Writable<Entity> writable = writableFactory.createWritable("SheetName", Arrays.asList("ColName"));
	try
	{
		writable.add(new MapEntity("ColName", "test"));
		writable.add(new MapEntity("ColName", "qwerty"));
	}
	finally
	{
		writable.close();
	}	
}
finally
{
	writableFactory.close();
}
```
  