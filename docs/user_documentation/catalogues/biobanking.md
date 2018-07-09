#Biobanks

This is a 'biobank-in-a-box' cloud service where one or more biobanks can manage and provide access to their sample information and research data sets. The system is 'multi-tenant' such that multiple biobanks can be hosted without interference with each-other unless they want to collaborate. In addition, the system provides an overarching catalogue feature such that it can also be used to establish institutional or (inter)national data catalogues. The system is MIABIS compliant and implements the BBMRI-ERIC 'directory' service so that biobanks and their contents can be discovered in international catalogues if desired.
[Discussion: do we want in the same system to also provide 'research projects' where individuals can collaborate around package of data sets?].
			
Persona involved:
* biobanker - the biobank staff responsible for coordinating a biobank
* data manager - the biobank staff responsible for data access
* researcher - the individual interested in using the material/data for research
(future: participant - the individual who donated material and/or data)

Relevant tools:
* upload - to batch upload data using Excel, TSV files or SPSS(future)
* edit - to online edit and enter data
* search - to search, view/count, filter and download information you have permission on
* request - to find and request data/sample items you don't yet have permission for
* provide - to accept a request and create an anonymised study data set
* integrate - to pool data from different datasets together
* manage - to manage and create your data / sample collection tables and permissions
* admin - for the system administrators
(future: questionnaires - for participants to enter additional data)
(future: mybiobank - for participants to view their own data)

# Data management

As data manager I want to enter research ready datasets and sample collections as basis for access to researchers. For this MOLGENIS / biobank contains a MIABIS compliant data schema that can be expanded and customized if desired. Data entry can be done using two tools. Either one uses the 'upload' tool [link to manual here] to import in bulk. Alternatively, one can use the 'edit' tool [link to manual here] to enter information by hand, e.g. as part of the data collection process. 

 data sets en sample collections using a simple Excel format based on a Alternatively, 

The basic information is organized in the following entities:

## Biobanks

Each row contains a summary description of the biobank and contact details (usually only one). For example 'LifeLines' is a biobank in Groningen that can be contacted via http://lifelines.net. The database may contain information about multiple biobanks, each with their own data manager. You can set permissions which user(group) can see what biobank.

## Participants

Here you can enter information per individual participant using a pseudonym identifier (this is about research data; in light of ELSI constraints you must store identifiable personal information in a separate relationship management system). For example 'll0001' is a participant of sex 'M' [todo: what is minimal information?]. Each participant is part of one biobank (cross biobank links can only be made after permission). Default to view/manage the participant is derived from the permission on the Biobank but can be changed.

## Sample collections

This is summary description of a group of samples that belong together based on for example contact details and access permissions. Each sample collection is part of one biobank. For example 'LifeLines deep' is a sample collection of material type 'feaces' which is collected as part of LifeLines biobank and access to can be requested by email to Cisca Wijmenga.

## Samples

Here each row describes a particular sample, part of one sample collection and optionally linked to participant. For example 'LLD1' for is a sample of type 'feaces' in LifeLines deep collection. 

Optionally, you can add custom attributes to describe additional information, e.g. show additional attributes depending on sample type. For example, for 'DNA' sample we want to add information like 'blabla'.

[Discussion: I assume we want to work with one big 'Samples' table where we use 'compound' to add additional custom attributes (that could be shown/hidden depending on sample type). Alternatively, we could create additional detail tables that could be linked in using xref view expansion?]

## Observation collections

Data collections are similar to sample collections in that they describe a set of data. For example, 'LifeLines visit1 children' contains is the observations collected using the 'children' questionnaire as part of the first collection round of LifeLines participant survey.

[ideally: for each row here we create 

## Observations

Each observation relates to a Participant [Discussion: or to a sample?]
[Discussion: I would expect I would like to have a separate table per Observation protocol]

## Code lists
We provide standard code lists
* MaterialTypes
* Diagnoses

# Search biobanks

Biobank data on humans is typically not public access but requires an access request where is judged 