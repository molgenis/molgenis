# Quick data import
The Quick data import allows you to easily get your data into MOLGENIS, without the need of understanding the EMX format.
There are some assumptions we make with regards file types and file contents.

1. The supported file types are _CSV_, _XLS_, _XLSX_, and a _ZIP_ file containing _CSV_ files
2. A file consists of a at least two lines, the first is the header, the rest is data
3. We always expect the first line to be the header 

- __Empty files can not be imported__
- __Files containing only a header can not be uploaded__
- __You need a package you can write content into, usually the package of the group you are a member of__

## Metadata guessing
The quick data import tries to guess what type your data is. 
MOLGENIS supports multiple data types like booleans, date fields, numbers etc.

1. To get __INTEGERS__, use values like _1_, _30_, _2012_, etc...
2. To get __LONG__, use values like _128938972487837384_
3. To get __DECIMAL__, use values like _2.3_, _2901.3_, or _123.8_
4. To get __DATE__, submit strings in the format of _dd/mm/yyyy_
5. To get __BOOLEAN__, submit strings in the format _TRUE_ or _FALSE_
6. Other values are inserted into the database as __STRING__. If a piece of text is longer then 255 characters, we use __TEXT__

## Data structure
MOLGENIS has an explicit data structure within the application. You need to know the basic terminology to understand how the data is structured.

### Terminology
In this section we introduce and explain the terminology concerning data structure of MOLGENIS.

* Groups: a group of people who manage data within one package (folder). 
* Package: Each group has a root package where it can store its data.
Packages can have child packages to logically subdivide that root package into a tree structure, 
like folders on a hard drive.
* Entity Type: An entity type is the metadata of a data collection, like a table in a database.
* Entity: The actual data that is collected based on the template from an entity type, like a table row
in a database.
* Attribute: An attribute describes the characteristics of a data item in an entity type, like a column 
in a database

Data is imported into the MOLGENIS database as a single _entity_ (table)
_entities_ are grouped within packages (folders)
Your entities are stored within a package that you can write content into, usually your "group" package

The base folder in which all other entities and packages are placed is dependent of the group you are part of.

1. In the case of an excel, the file name is used as the _package_ name and the workbook sheet is used as the _entity_ name. Packages will be created as children of the first writable package, usually the package of the group you are in.
2. In the case of a CSV, the file is used as the _package_ and the _entity_ name. The package will be created as child of the first writable package, usually the package of the group you are in.
3. In the case of a ZIP file, the name of the ZIP file is used as the _package_ name, and the names of the files inside the ZIP are used as the names for the _entities_. The package will be created as child of the first writable package, usually the package of the group you are in.

You can move the package to the location you want after the import is done.

## How to use
1. Click upload file
2. Select a file
3. Wait...
4. Done!

There are two types of links:
1. The file name will send you to the MOLGENIS navigator. Here you can view all packages, and the corresponding data tables.
2. The nested links (sheet names for Excel, file names for CSV) will take you to the data explorer. 
Here you can view, filter, query, download, and share your data