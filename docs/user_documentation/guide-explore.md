**
One of the central plugins for many of the MOLGENIS databases is the data explorer. As the name suggests this is the plugin to use if you wish to take a close look at your data.
**

Note that some of the components described below are only shown if they are enabled in the application settings and to people with the appropriate permissions.

# Select entity
At the top left corner of the data explorer, the name of the currently selected entity is shown, as well as the description (if given).

At the top right corner, a dropdown (entity select) is shown which can be used to select the entity you wish to display.
For admins, a delete button is shown at the right side of the entity select. Clicking it will allow you to choose if you only want 
to delete the data or also want to delete the metadata for the currently selected entity.
  
![Dataexplorer entity select](../images/dataexplorer/entitySelect.png?raw=true, "dataexplorer/entitySelect")


# Search/Filter
In the upper left corner of the data explorer, below the name of the entity, a search box is shown.
This search box can be used to search all your data for a certain search term.
  
![Dataexplorer search](../images/dataexplorer/searchbox.png?raw=true, "dataexplorer/searchbox")

Directly below the search box, the currently active attribute filters are shown. 
They can be edited by clicking on them. The cross trailing can be used to delete the currently used filter(s). 
Filters can be set from the attribute selection tree which is described below. 
Using the checkbox in front of each attribute, the visibility of this attribute in the table can be managed. 
The filter icon can be used to set filters for this specific attribute.
  
![Dataexplorer active filters](../images/dataexplorer/active_filters.png?raw=true, "dataexplorer/active_filters")

In the area with the active filters, you will also find the button to open the filter wizard. 
This is a popup screen that allows you to add filters for different attributes in one go.

![Dataexplorer filter wizard](../images/dataexplorer/filterwizard.png?raw=true, "dataexplorer/filterwizard")

Any filters you use are stored in the URL as an RSQL query. This allows you to bookmark pages with certain columns and filters selected inside a specific table.
For more information on RSQL operators, you can take a look at our [RSQL operator](../developer_documentation/ref-RSQL.md) documentation.

# Different views
    
![Dataexplorer tabs](../images/dataexplorer/dataexplorer_tabs.png?raw=true, "dataexplorer/dataexplorer_tabs")

The data explorer consists of multiple modules to view or process the data in different ways. These modules are described below:

# Data table
The data module shows the data in a table. This table can be sorted by clicking on the headers.
If your data contains references to other entities, an icon is shown to expand this reference to show the data from the referenced entity.

Every line starts, depending on your permissions, with some action icons:
  
![Dataexplorer action buttons](../images/dataexplorer/action_buttons.png?raw=true, "dataexplorer/action_buttons")

- Add row:
Using this button, a form will open with input for all the attributes in the entity, allowing you to create a new row.
All fields will be validated based on their datatype. For example, "test" is not a valid email address.
- Edit row:
Same as the add row button, but with prefilled inputs to allow you to edit a row.
- Inspect row:
This button will open a form with a custom made report for this row. With the right permissions, different reports can be created using the FreemarkerTemplate entity.
- Delete row:
This button can be used to remove a row from the entity. 

### System packages, system entities and system attributes

System packages, system entities and system attributes are available in MOLGENIS 2.0 after installation. They are configured programmatically and can only be changed in new releases of the software. Changing the system packages, system entities and system attributes via the metadata editing functionality, will be registerd in the database and update MOLGENIS (not the java part). When dropping the index and database, this registration will be dropped, and the original values will be loaded again.

# Download  

At the bottom right of each table, there is a download button. This button allows you to save the data to a CSV or XLSX file. The XLSX download is limited to 500000 values to prevent performance issues. 
Depending on the purpose of the download, identifiers or labels can be used as column headers.

# Genome browser
  
![Dataexplorer first screen](../images/dataexplorer/genome_browser.png?raw=true, "dataexplorer/genome_browser")

MOLGENIS provides genome browsing using the[Dalliance](https://www.biodalliance.org/)genome browser.

If the genome browser should be shown at all in a MOLGENIS instance, the browser can be configured via the data explorer settings.

By default entities containing attribute combinations specified in the "GenomeBrowser Attributes" table are shown.
It is possible to add/remove or update those entities to change which entities should show the genome browser.

It is also possible to add specific configuration for an entity by adding a row in the "GenomeBrowser Settings" table:

**Label**	a human readable label for the configuration.

**Entity**	the entity for which this is the configuration

**Genomic attributes**	reference to the "GenomeBrowser Attributes" row that should be used for this dataset	

**Label Attribute**	the attribute that should be used as a label in dalliance	

**Track type**	the type of track, choice from:
- VARIANT: tracks are shown in letters (alternative allele), stars (if more than one alternative allele) or dots (indels)
- EXON: tracks are shown in white(intron) and red(exon) blocks with an label
- NUMERIC: tracks are shown as a histogram for every row

**Exon/intron key**	keyword to distinguish between exons and introns based on the label value, if the keyword is avaialble we assume it is an exon
	
**Score attributes** attribute name of the column containing the value that should be used for a numeric track 	

**Feature popup attributes**   attributes that should be shown in the dalliance feature popup, format: attributename:label,attributename,label	 

**Reference track mode**	 
If additional molgenis datasets should be shown in the genome browser when this one is selected, possible values: 
- ALL: all tracks that qualify to be shown in the browser are shown
- CONFIGURED: additional tracks are configure in the "molgenis_reference_tracks"
- NONE: no additional molgenis tracks will be shown
	
**Reference tracks**	The other tracks that should be shown if this one is selected, this is a reference to other "GenomeBrowser Settings" rows	

**Actions**	Javascript functions to be added to the dalliance feature popup. Actions are executed when the link in the feature popup is clicked. Actions should be provided in an javascrip object with 'label' (which will be used as the link) and 'run', example: *[{label:"action1" run:"alert('action1')"},{label:"action2" run:"alert('action2')"}]*.

## Try it out

Upload the [vcf_example_file](/data/Documentation_VCF.vcf "VCF example file") using the importer.
Let's select an entity containing genomic variants, by selecting the entity name you just chose for the upload in the entity select.
Let's assume we have a specific location we are interested in, say position 103214569 at chromosome 7. We'd like to search for that specific line in the entity.
Let's first use the search box to see if we can find the line that way:
enter "103214569" in the search box and press enter.

But now we like to take a look at all variation on chromosome 7. As you can imagine searching for "7" in all the attributes in the data, will give us a lot of results we are not looking for. So we'd like to filter for this value specifically for the chromosome attribute.

Click on the filter icon in front of "#CHROM" in the attribute selection tree, and enter "8" in the input field. Then click "apply".
However, we meant to search for chromosome 7, so let's click the filter in the active filters box, and change the value to 7.
We now have all the values for chromosome 7 in the table, however, the results are divided over several pages of results. We'd like to view them all in one screen; click the "rows per page" dropdown below the table, and select "100". This will show 100 results per page.
The "FILTER" column shows the same value for every line. Since we are not interested in this column, let's hide it by clicking the checkbox in front of "#CHROM" in the attribute selection tree.

Click any column header in the table to sort the data based on that column, click again to sort in the opposite direction.
Click one of the lines in the table to zoom to the position of this variant in the genome browser.
Click the symbol in front of the "SAMPLES" column header to show the columns belonging to the samples.

Click the magnifying glass in front of the data line, to show a report for that line. The default report just shows all attribute values in a structured way. However, as stated above, all kinds of reports can be added at runtime.

# Inline editing

Click the edit icon and change the chromosome from 7 to 8 and save.
Adding a row works the same way, only without the prefilled fields.
Now, let's click the red garbage bin icon in front of a line to delete this line from the entity.

TODO: add figure

# Annotators

The annotator module of the data explorer is the user interface to use the MOLGENIS annotator framework, which can also be used from the command-line.
The annotator framework is a system to add data from other resources to your genomic entities. For example pathogenicity prediction, allele frequencies and phenotype information.

![Dataexplorer annotators](../images/dataexplorer/annotators.png?raw=true, "dataexplorer/annotators")

The screen shows a list of available annotators that can be used. Clicking the title of the annotator will result in a popup with additional information, such as a general description and a listing of the attributes that will be added by this annotator.
Using the checkboxes, multiple annotators can be selected for one run, which is started by clicking the "annotate" button. If preferred, a copy of the dataset can be created with the annotations added to this copy, leaving the original entity as it is.
Annotated fields will be added to the entity in a compound attribute.

On the right hand side of the screen, a list of unavailable annotators is shown. The reason why they are unavailable is shown in this list. This can for example be due to a resource being unavailable, or when an attribute needed to map the entity and resource to each other is missing.
The gear icon trailing every annotator in the list, can be used to configure the settings for this annotator.

# Aggregation

The aggregation module allows you to produce aggregated counts for queries on the data.

The screen has 2 areas, the controls and the results. The controls allow you to choose the attributes you wish to use for the aggregation.

![Dataexplorer aggregates](../images/dataexplorer/aggregate_controls.png?raw=true, "dataexplorer/aggregates")

Youcan select 1 attribute for simple one dimensional counts, represented as a table with one column, or two attributes to get a 2 dimensional aggregate table.
A third dropdown allows you to select an attribute by which to group the results.

![Dataexplorer aggregate results](../images/dataexplorer/aggregate_result_table.png?raw=true, "dataexplorer/aggregateresults")

These functionalities are best explained by the example in the "try it out section below".

## try it out
Upload [emx_example_file](/data/Documentation_EMX.xlsx "EMX example file") through the importer.
Navigate to the data explorer and select the aggregates tab. Select the just uploaded "biobanks" entity.

Now select "patientid" in the entity dropdown.
You now get a 1 dimensional list of counts, showing you that every patient has 3 entries in the selected entity.

Now select "biobank" in the first aggregate dropdown, and in the second select "sampletype".
You now get a table representing the amount of samples in both biobanks per type.

Finally, select "patientid" in the third dropdown, the distinct attribute. 
The table will update to show you how many patients with at least one sample of a specific type are available in a biobank.

# Reports
The reports functionality is made for overriding the default instance view or to add an instances tab in the Data-explorer. Overriding the views or adding a tab is possible by creating a new `FreemarkerTemplate` entity with the right name convention. This short tutorial will show you how to achieve this.

There are two ways to create your own reports: 

1. Overriding the default instance view.
2. Add one or more instances view tabs.

You will need:

1. A data set: "Advanced data example" ([download](/data/advanced_data_example_v20171206.xlsx)). Upload this dataset into your MOLGENIS instance, see the [Upload guide](guide-upload).

###### Override the entity view

Steps:

1. Go to the Data Explorer.
2. Select the "cities" entity via the entity select dropdown.
3. The entity view modal is opened when you click on the ![View entity report button](../images/reports/view-entityreport-button.png?raw=true, "Entity view") button".
4. The default view will be: ![View entity report default](../images/reports/default-entityreport-view.png?raw=true, "Entity view")
5. Let's upload our own template. 
	a. Go to the data explorer and select the FreemarkerTemplate entity.
	b. Click on the ![add](../images/add.png?raw=true, "add") button. In the modal you fill in:
		* Name: view-entityreport-specific-root_hospital_cities.ftl (view-entityreport-specific-\<Full entity name>.ftl)
		* Value: "\<div>City name: ${entity.get('cityName')}\</div>"
	![view-Cities-entitiesreport](../images/reports/view-entityreport-specific-root_hospital_cities.png?raw=true, "view-Cities-entitiesreport")
6. Repeat steps 2 and 3. 
7. The new view will be: ![View entity report custom](../images/reports/custom-entityreport-view.png?raw=true, "Entity view")

###### Add an instances view tab
1. Go to the data explorer and select the "cities" entity through the entity select dropdown.
2. Let's upload our own template. 
	a. Go to the data explorer and select the FreemarkerTemplate entity.
	b. Click on the ![add](../images/add.png?raw=true, "add") button. In the modal you fill in:

Name: ```view-Cities-entitiesreport.ftl (view-<template name>-entitiesreport.ftl)```

Value: 
```javascript
<link rel="stylesheet"
      href="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/leaflet.css"/>

<div id="map" style="width: 600px; height: 400px"></div>

<script>
    function showMap() {
        var map = L.map('map').setView([38, -80], 4);

        L.tileLayer('//api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6IjZjNmRjNzk3ZmE2MTcwOTEwMGY0MzU3YjUzOWFmNWZhIn0.Y8bhBaUMqFiPrDRW9hieoQ', {
            maxZoom: 18,
            attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
            '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
            'Imagery &copy; <a href="http://mapbox.com">Mapbox</a>',
            id: 'mapbox.streets'
        }).addTo(map);

    <#list datasetRepository.iterator() as city>
        L.marker([${city.get("lat")},${city.get("lng")}]).addTo(map)
                .bindPopup("${city.get("cityName")}").openPopup();
    </#list>
    }
    $.getScript("//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/leaflet.js", showMap);
</script>
```
		
		![view-Cities-entitiesreport](../images/reports/view-Cities-entitiesreport.png?raw=true, "view-Cities-entitiesreport")

3. Click on the settings icon ![Settings](../images/settings.png?raw=true, "Settings")
	a. Check: Modules -> Data -> Reports -> Yes
	b. Set: Reports -> Reports -> root_hospital_cities:Cities
		* root_hospital_cities is the entity name.
		* Cities is the template name.
	
	![Entities report settings](../images/reports/entities-report-correct-settings.png?raw=true, "Entities report settings")
4. Refresh the page and repeat step 1.
5. The result:![Custom entities report](../images/reports/custom-entities-report.png?raw=true, "Custom entities report")