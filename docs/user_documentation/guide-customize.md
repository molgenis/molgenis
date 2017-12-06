**
MOLGENIS allows extensive customization of look & feel to suit your work
**

# Menu
The menu manager is a module that gives you the ability to arrange your menu to contain the items that you want it to contain. If you only want to show the data explorer and importer, you can do that. If you want to throw away news and background modules because you do not have any need for them, then you can do that as well. Every item is configurable.

![Menu manager screen](../images//menu_manager.png?raw=true, "menu manager")

Just remember that throwing away the menu manager module might not be the best idea! And it's good to always keep the blue Home item at the top, else your homepage will no longer be accessible to anonymous visitors.

## Try it out 
When you enter the menu manager screen, it can be a bit overwhelming. But do not be alarmed, we will take you through it one step at a time.

First, pay attention to the large list on the left. As you can see, this block represents the current menu structure. You can change the structure by moving items around, you can change the name of a menu item by pressing the pencil, and you can remove items by pressing the trash can. Remember to save your changes before leaving this screen by pressing the 'Save the new menu layout' button. If you do not, then your changes will not be applied to the menu.

The block to the right gives you the options to add new menu items, or to add an entirely new menu. Try it by creating a new menu with 'test_plugin' as ID and 'Test' as the Name, then press 'Create'. You will see that a new item, called Test, is added to your list of items on the left. Now that you have created a new menu, let's add an item to that menu.

Fill in the Create Menu Item form with the following data:

* Plugin: contact
* Name: Contact information
* Query string: 

Now press 'Create'. The Contact information item will appear in the list on the right. Move it under the Test menu, and save the layout. You should now have a Test drop down in your menu, and when you open it, it should show you the Contact information item. The contact plugin is similar to the Home plugin as it lets you fill in information via an online editor

**Using Query string to add additional parameters**  
Some modules, like the data explorer, can be opened with starting parameters. These can be used via the Query string field when creating a new menu item. To test this, we will add a Query string to the existing Data Explorer menu item so that the data set we created in the [Upload guide](guide-upload)will be selected at the start.

Edit the existing data explorer item by pressing the pencil, and add the following Query string:

* Query string: entity=example_data_table

Save and when you now press the *Data explorer* in your menu then you will be taken to the data explorer with the example_data_table data set selected.

A complete list of all the Query strings available at the Data Explorer:

|Query string |Options |Use |Example |
|-------------|--------|----|--------|
| ***entity*** | All existing entities |Using this query string you can open the data explorer with the specified entity selected. |entity=test_data|
| ***hideselect*** | true, false|Hide the drop down for selecting data sets. Use this if you want users to focus on only one data set. Combined with the ***entity*** query string, you can create a Data Explorer that only shows one 	data set to users.|entity=test_data&hideselect=true|
| ***mod***|data, aggregates, annotators|Select a Data Explorer tab to show|entity=test_data&hideselect=true&mod=data|

**Creating redirects to URLs outside MOLGENIS**  
Using the redirect plugin as a menu item, you are able to create a link to an outside source. To show how this works, we will create a menu item that links to Wikipedia.

First, create a new Menu item that has the following traits:  

* Plugin: redirect
* Name: Wikipedia Link
* Query string: https://en.wikipedia.org/wiki/Bioinformatics 

Second, move the new link below the Home item. Then press the 'Save the new menu layout' button. 

A new menu item will appear which will take you to the bioinformatics Wikipedia page.

# Style/theme
Creating your own research database often comes with the desire to add a unique styling to it. We wanted to give users a way to change the standard [Bootstrap](http://getbootstrap.com/) style that is shipped with MOLGENIS. 

Using the Theme manager, you can select between multiple bootstrap styles.

![Theme Manager](../images//theme_manager.png?raw=true, "Theme Manager")

To get the feel of how certain styles look, you can select it in the dropdown menu. The style will not be applied to the application unless you press the 'Save current theme' button.

For an overview of all the different themes offered, visit the [Bootswatch](https://bootswatch.com/) website. 

### Adding a new theme
It is possible to submit your own CSS sheets to update the styling. By selecting the 'Add theme' option a new theme file may be added. As molgenis currently uses both bootstrap version 3 and bootstrap version 4 (alpha) classes for styling the interface, theme's for both versions may be added. It is mandatory to add a bootstrap version 3 theme file. Adding a bootstrap version 4 theme file is optional (but recommended). If a theme is selected for which no bootstrap 4 theme file was set, the 'default' bootstrap 4 theme will be used.

# Homepage
The home page is one of the most important part of your MOLGENIS application. It is a doorway to the rest of your application, and as it is such, it should look nice! When you are logged in as administrator, the home page contains an *edit* button. Clicking this button will open up an editor. 

![Home page editor](../images//home_page_editor.png?raw=true, "home page editor")

Here you can do simple stuff like adding text, images, and table. 

If you want more control, you can go to tools -> Source code. This opens up an HTML editor, which is nice if you know how to write HTML. For even more fancy functionality, the Source code editor also allows you to write JavaScript code.

If you need some aspiration for a nice home page, you visit either the [ASE website](http://molgenis.org/ase) or the [COL7A1 website](https://molgenis03.target.rug.nl/).

# Entity report

By default, the entity report will be generated using the generic [Freemarker](http://freemarker.incubator.apache.org/docs/index.html) template [`view-entityreport-generic-default.ftl`](https://github.com/molgenis/molgenis/blob/master/molgenis-dataexplorer/src/main/resources/templates/view-entityreport-generic-default.ftl). You can customize this entity report for specific entities by adding Freemarker Templates for those entity. You'll need write permission for the entity `FreemarkerTemplate` to do this. 

1. In the top menu, select the Data Explorer.
2. In the pulldown, select the entity FreemarkerTemplate
3. Push the plus icon.
4. The Name is `view-entityreport-specific-<the entity name>.ftl`. So for an entity named `ASE`, the Freemarker Template containing the entity report should be named `view-entityreport-specific-ASE.ftl`
5. The Value is a [Freemarker](http://freemarker.org/) Template specifying the contents of the report.

The HTML you specify in the template will be shown in a modal dialog, so you'll usually want to add a header, a body, and a footer:

```html
<#-- modal header -->			
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h4 class="modal-title"><#-- Custom entity report title goes here --></h4>
</div>

<#-- modal body -->
<div class="modal-body">
    <#-- Custom entity report contents go here -->
</div>
	
<#-- modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>
```

# Entity report in dataexplorer tabs
If you use the dataexplorer, you might have noticed the tabs showing data or aggregates. MOLGENIS allows you to create tabs with your own 
content. This content support both HTML and JavaScript.

To Create your own data explorer tab, do the following:
1. In the top menu, select the Data Explorer.
2. In the pulldown, select the entity FreemarkerTemplate
3. Push the plus icon.
4. The name is `view-<report name>-entitiesreport.ftl`. So for a report called example, the template would be called `view-example-entitiesreport.ftl`
5. The value is a [Freemarker](http://freemarker.org/) Template specifying the contents of the report.

```html
<h1>Example</h1>
<p>
    This shows you an example of 
    a report as a tab in the dataexplorer
</p>
```

Now that you created a freemarker template, you want to associate this template with your data set:
1. Go to the dataexplorer and select the cogwheel in the top right corner
2. Scroll down to the reports section
3. In the text area, type the following: `<my data set name>:<report name>`. For our report, we have a data set called MyData. To link our previously created template, fill in `MyData:example`.
4. Save, wait for the page to reload, and select MyData from the dropdown in the top right corner.
5. You will now see a new tab called example, which contains the HTML you filled in in the freemarker template.

# Standalone entity report
In some cases it would be nice to be able to share a row in your data with someone else. Therefore MOLGENIS contains the “standalone report” functionality. This can be set in the dataexplorer settings. To go there, go to the dataexplorer and click on the gear below the logout button.
![Setting cog](../images/customize/setting_cog.png?raw=true, "setting cog")

In section “Modules”, subsection “Data” the setting “Standalone Reports” can be found. By default the “Standalone Reports” are turned off. 
![Dataexplorer settings](../images/customize/dataexplorer_settings.png?raw=true, "dataexplorer settings")

When turning them on, keep in mind that when customizing your entity report in the FreeMarkerTemplate table (see above), you also need to add a customized “view-standalone-report-specific-*Your entity name here*.ftl”. This file should contain what you want to show in your standalone report. 
![Freemarker table](../images/customize/freemarker_table.png?raw=true, "freemarker table")
![Standalone Report form](../images/customize/standalone_report_form.png?raw=true, "standalone report form")

With the configuration being completed, clicking on the magnifying glass button in front of a row in your dataset will result in your specific freemarker template with the addition of a share link in the upper right corner. 
![Share URL](../images/customize/share_url.png?raw=true, "share url")

When clicking the copy button, the URL is copied to your clipboard. You can paste it in your address bar in the browser to go to the specific entity report of that row. This way you can bookmark it and share it with others. Opening the link shows the content of your entity report on a new page. Clicking the blue button with: “View full dataset” leads back to the specific table in the dataexplorer. 
![Standalone view](../images/customize/standalone_view.png?raw=true, "standalone view")

[Download the example dataset here](../data/standalone_report_data_example.xlsx)
[Download the freemarker template example here](../data/view-standalone-report-specific-root_hospital_patients.ftl)

## Accessing entity details from Freemarker
The model will contain
 * a variable named `entity` of type [`org.molgenis.data.Entity`](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/Entity.java) 
 * a variable named `entityMetadata` of type [`org.molgenis.data.EntityMetaData`](https://github.com/molgenis/molgenis/blob/master/molgenis-data/src/main/java/org/molgenis/data/EntityMetaData.java)
which you can use to specify the contents of your template.

* To display the value of one of the entity's attributes value, use `${entity.getString("<the attribute name>")}`.
* To make a conditional check on the actual value of an attribute, use `<#if entity.get("P_Value") == 0>...</#if>`.
* You can follow references to other entities: `<#list entity.getEntities("Genes") as gene>...</#list>` will iterate over all Genes. In between the list tags, the gene gets added as a variable `gene` which you can access the same way as you access the variable `entity`.
* To open a page at a button click, use the `data-href` attribute to specify the href: `<button type="button" class="btn" data-href='<the link>'>Button label</button>`.
* These are just examples, many powerful constructs are possible.

### Adding genome browser component

You can show the genome browser, zoomed in on this specific entity.

In the body, add a placeholder div for the genome browser:
    `<div id="modalGenomeBrowser"></div>`

At the bottom of the template, add script tags to instantiate the genome browser:

```javascript
<script>
    molgenis.dataexplorer.data.createGenomeBrowser({
        pageName: 'modalGenomeBrowser', 
        noPersist: true, 
        chr: ${entity.getString("Chr")}, 
        viewStart: ${entity.getString("Pos")} - 10000, 
        viewEnd: ${entity.getString("Pos")} + 10000
    });
    
    setTimeout(function(){
        $('.modal-body').animate({scrollTop:0},0);
    }, 10);
</script>
```

# Internationalization
## Adding a new language
1. In the top menu, select the Data Explorer.
2. In the pulldown, select the entity 'languages'.
3. Push the plus icon.
4. The code must be a lowercase ISO 639 alpha-2 or alpha-3 code.
5. The name is the name of the language as shown to the user.

If the is more then one language available a dropdown appears next to the 'Sign out' button
on the right of the menu bar (Maybe you have to refresh the browser page first). You can switch langage by selecting a language
from the dropdown.

## Editing translations
1. In the top menu, select the Data Explorer.
2. In the pulldown, select the entity 'i18nstrings'.
3. Click on the 'Edit row' icon before the row you want to edit.
4. Provide a translation for each available language.

Missing translations will show up in English in the user interface.

# Adding plugin descriptions
1. In the top menu, select the Data Explorer.
2. In the pulldown, select the entity 'Localization'.
3. Push the plus icon.
4. Fill in the form according to which plugin you would like to add a description to.

Example for the importwizard plugin, the pluginId is 'importwizard'. 
The plugin ID can be found in the URL, the sys_Plugin table, or in the menu manager.

```
	msgid: [pluginId]_description_text
    	namespace: [pluginId]
    	description: the i18n entry for the importwizard description text
    	en: Import wizard. Use the EMX format to create excel files for import
```

After saving, the importwizard plugin will now have a description at the top when the english language is selected.
This is an admin only feature and allows the admin to use HTML.

# Adding a banner
If you want to brand your MOLGENIS instance, you could add a banner.
We recommend high resolution image. It will be automatically resize to fit the browser window.
Do realize that taking a banner with a very big height might make your application unusable.

There is an application setting that allows you to set the max height of your image.
This will force an image to have a height according to the set amount of pixel.

An example of a banner:
![Website banner](../images/customize/website_banner.png?raw=true, "website banner")