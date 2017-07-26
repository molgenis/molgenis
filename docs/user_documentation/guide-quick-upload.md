Quick import
------------

The Quick data import allows you to easily get your data into MOLGENIS, without the need of understanding the EMX format.
There are some assumptions we make with regards file types and file contents.

1. The supported file types are _CSV_, _XLS_, _XLSX_, and a _ZIP_ file containing _CSV_ files
2. A file consists of a at least two lines, the first is the header, the rest is data
3. We always expect the first line to be the header 

- __Empty files can not be imported__
- __Files containing only a header can not be uploaded__

Metadata guessing
-----------------

The quick data import tries to guess what type your data is. 
MOLGENIS supports multiple data types like booleans, date fields, numbers etc.

1. To get __INTEGERS__, use values like _1_, _30_, _2012_, etc...
2. To get __LONG__, use values like _128938972487837384_
3. To get __DECIMAL__, use values like _2.3_, _2901.3_, or _123.8_
4. To get __DATE__, submit strings in the format of _dd/mm/yyyy_
5. To get __BOOLEAN__, submit strings in the format _TRUE_ or _FALSE_
6. Other values are inserted into the database as __STRING__. If a piece of text is longer then 255 characters, we use __TEXT__

Data placement
--------------

Data is imported into the MOLGENIS database as a single _data table_
_Data tables_ are grouped within packages

1. In the case of an excel, the file name is used as the _package_ name and the workbook sheet is used as the _data table_ name.
2. In the case of a CSV, the file is used as the _package_ and the _data table_ name.
3. In the case of a ZIP file, the name of the ZIP file is used as the _package_ name, and the names of the files inside the ZIP are used as the names for the _data tables_

How to use
----------

1. Click upload file
2. Select a file
3. Wait...
4. Done!

There are two types of links:
1. The file name will send you to the MOLGENIS navigator. Here you can view all packages, and the corresponding data tables.
2. The nested links (sheet names for Excel, file names for CSV) will take you to the data explorer. 
Here you can view, filter, query, download, and share your data