# Navigator

The "Navigator" plugin enables finding resources (e.g. packages and entity types) as well as manage them.
Resources can be created/edited, moved/copied, uploaded/downloaded and deleted.

![Navigator_screen](../../images/navigator/navigator.png?raw=true, "navigator/screen")

## Find
Use the search box to find resources. Search results are displayed in the table. It is possible to 
select search results and move/copy or download the selection. 

![Navigator_screen](../../images/navigator/navigator-find.png?raw=true, "navigator/screen")

Clear the search box to return to the resource tree.

## Create and Edit

### Create
The 'create' dropdown button allows for creation of new resources.
 
![Navigator_screen](../../images/navigator/navigator-create.png?raw=true, "navigator/screen")

Entity type creation is enabled for users with access to the metadata manager plugin.
 
### Edit
The 'edit' button allows to update a selected resource.

![Navigator_screen](../../images/navigator/navigator-edit.png?raw=true, "navigator/screen")

Entity type editing is enabled for users with access to the metadata manager plugin.

## Move and Copy
The Cut, Copy and Paste icons control the clipboard and can be used to move and copy resources.
Cutting selected resources places them on the clipboard. Subsequently selecting the copy/paste icon
will copy/move the clipboard resources to the current package.

![Navigator_screen](../../images/navigator/navigator-cut.png?raw=true, "navigator/screen")

### Move
Resources on the clipboard can be moved to another package by navigating to another package and 
selecting the paste icon.
  
![Navigator_screen](../../images/navigator/navigator-paste.png?raw=true, "navigator/screen")

### Copy
Copied resources on the clipboard can be pasted anywhere with the paste button. When copying one or
more packages, the contents of those packages will be copied as well. 

![Navigator_screen](../../images/navigator/navigator-copy.png?raw=true, "navigator/screen") 

If you are copying multiple entity types at the same time, references between those entity types 
will also be copied. References to resources that are not being copied at the moment will keep 
pointing to that resource. The following diagram depicts how references are copied:

![Navigator_screen](../../images/navigator/navigator-copy-diagram.png?raw=true, "navigator/screen")

## Upload and Download
The upload/download allow for adding resources to the current package or downloading selected resources.

### Upload
Uploading is enabled for users with access to the import plugin.

### Download
Packages and entities can be downloaded in EMX format by selecting them and clicking the download button.
A progress message will be shown on screen, this message will contain the link to download the EMX file once the download is ready.

Downloading is enabled for users with permissions to create download jobs and files in the system. 
By default this is true for users with the role "Manager" or "Editor" and superusers. 

#####Limitations
Only entities that follow the naming scheme "PACKAGE_ENITYNAME" as identifier can be downloaded.
Also the identifier cannot be longer that 31 characters due to sheet name length limitations in some older spreadsheet programs.

Tags on your metadata will not be downloaded, these need to be added to the file manually before it can be uploaded into MOLGENIS via the import plugin.

![Navigator_screen](../../images/navigator/navigator-download.png?raw=true, "navigator/screen")

## Delete
Select one or more resources and use the 'delete' icon to delete the selected resources.

![Navigator_screen](../../images/navigator/navigator-delete.png?raw=true, "navigator/screen")