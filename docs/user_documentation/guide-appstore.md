## The MOLGENIS Appstore
The MOLGENIS appstore allows you to write your own web-based applications and upload them as a 
MOLGENIS plugin at runtime.

## The main menu
When opening the Apps plugin for the first time, you will see an empty screen with a button for creating a new app.

Press the button to get the following screen

![App creation form](../images/appstore/app_creation_form.png?raw=true, "appstore creation form")

Start filling in the form

| Item        | Description | 
| ----------- | ----------- |
| Name        | The name of your app |
| Description | A description about your app |
| Icon URL    | A URL to an icon which will give some character to your app inside the appstore |
| Resource zip file | A bundle of HTML, JS, and CSS. These resources can be pointed at via the last form item. [Example app bundle](../data/app-valid.zip) 
| Landing page HTML template | The HTML page that will be opened on App click or via a Menu item. [Example HelloWorld HTML](../data/view-apps-helloWorld.ftl), [Example EntityTypeOverview HTML](../data/view-apps-entityTypeOverview.ftl)  

You can create a new HTML landing page by pressing the '+' button.

![HTML creation form](../images/appstore/html_creation_form.png?raw=true, "html creation form")

Example of a fully filled out form

![Filled form](../images/appstore/filled_form.png?raw=true, "filled form")

Now that you filled out the form you can activate and deactivate the app by pressing the activate / deactivate button

![Activate](../images/appstore/activate_deactivate_apps.png?raw=true, "activate / deactivate")


When an app is active, click on the icon to go to your previously created HTML landing page template:

![Landing page](../images/appstore/landing_page.png?raw=true, "landing page")

## Purpose of the App store
The appstore allows you to create your own web-applications, and place them inside any running MOLGENIS which has the appstore plugin.
If you want to create a custom web application to go with your custom data model, you can write your JS/HTML to display the data in such a way that it fits your needs.

Using REST api calls, you can access data that was previously imported, and use fancy JS magic to create the ultimate data visualisation apps

## Caution
Apps are stored in the database. If you do not properly version or backup your own apps, then a server crash or a database drop **WILL** delete your app.
Be smart, backup your apps.

We use [GitHub](https://github.com) for code management and versioning. We can highly recommend GitHub for any coding project you might start working on.


