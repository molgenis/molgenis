# Apps in MOLGENIS
To manage Apps in MOLGENIS you can use the MOLGENIS App manager. It is a tool to upload, search, and manage your apps. With a an easy to use format we want
to facilitate rapid app development on top of your own data models. 

How to create your own MOLGENIS app is described [here](../../developer_documentation/app-development.md)

The following paragraphs will give a brief tutorial on the App manager and its functions.

## Uploading your first app
We will be using the [example app](../../data/molgenis-app-example.zip) to go through this tutorial.

When you first go to the app manager, you will probably be greeted by an empty screen with the message that no apps were found.

![am01 - empty screen](../../images/app-manager/am01-empty-screen.png?raw=true, "am01-empty-screen")

Lets change that now. Click the big green __Upload new app__ button, and select your app archive.

![am02 - upload file](../../images/app-manager/am02-upload-file.png?raw=true, "am02-upload-file")

Your result is an app in the app manager, ready to be managed!

![am03 - after upload](../../images/app-manager/am03-after-upload.png?raw=true, "am03-after-upload")

##### Note
If anything is missing inside your archive or important configuration is missing from your config file, 
the app will not be uploaded and you will get a summary of missing files or parameters.

## Manage your first app
As you can see, the newly uploaded App will be shown as an app card. 
This card gives you a brief overview about the status and contents of the app.

### Activating your app
New apps always start out as inactive, meaning they can not be accessed yet, or be added to the MOLGENIS menu.

Apps which are inactive can be deleted. Apps which are active can __not__ be deleted.

Lets use the toggle button now to activate our example app.

![am04 - after activate](../../images/app-manager/am04-after-activate.png?raw=true, "am04-after-activate")

Activating an app does two things:
* Set the status of an app to active in the database
* Add an app to our internal Plugin table, allowing for management via the [Menu manager](guide-customize.md), or setting permissions via the [Permission manager](./guide-admin.md#permissions)

### Add app to menu and view
Lets add it to the menu and open the app

![am05 - open app](../../images/app-manager/am05-open-app.png?raw=true, "am05-open-app")

The content is a bit minimal but, congratulations! You just added your first app.

### Searching for apps
Going back to the app manager screen, we have two small pieces of functionality left. 
Once you get a lot of apps, you can use the search function to quickly find apps that might be relevant to you.

We just imported a lot of them 

![am06 - before search](../../images/app-manager/am06-before-search.png?raw=true, "am06-before-search")

and now we are interested in apps having to with Biobanks or Bio

![am07 - after search](../../images/app-manager/am07-after-search.png?raw=true, "am07-after-search")


### Deleting an app
Once we want to update an app, we will have to disable and remove the older one first.
Toggle our example to inactive, and delete!

![am08 - delete app](../../images/app-manager/am08-delete-app.png?raw=true, "am07-delete-app")

You are now free to make changes to the config, descriptions, name, and resources.
After making your changes you can upload the archive again and use the app as you see fit!
