# Biobank Directory

The user can browse a directory of biobank collections in the Data Explorer.
Filters can be used to find relevant collections.
A particular combination of query filters can be sent to a biobank Negotiator to start a negotiation with selected biobanks.

If the user selects the configured collections entity and has the proper permissions, a button will be shown
labeled `Export to BBMRI Negotiator`. If the user pushes the button, MOLGENIS will create a Query on the Negotiator and
with the selected biobank collections and filters and then forward the user to the Negotiator where the user
can log in and proceed with the query.

The user can return to the Biobank Directory from the Negotiator to edit this selection.

## Permissions needed
If you want to allow (anonymous) users to browse the biobank collections, you need to give them
the following permissions in the Permission manager:

### Plugin Permissions
|Plugin        | Permission |
|--------------|------------|
| dataexplorer | View       |
| directory    | View       |

### Entity Class Permissions
| Entity Class                 | Permission |
|------------------------------|------------|
| *\<Your collections entity>* | Read       |
| *\<Your biobanks entity>*    | Read       |
| sys_md_Attribute             | Read       |
| sys_md_EntityType            | Read       |
| sys_md_Package               | Read       |
| sys_set_dataexplorer         | Read       |
| sys_set_directory            | Read       |

## Configuration
The integration with the negotiator can be configured in the *Negotiator Config* and *Negotiator Entity Config* in the *Data Explorer* plugin.

The *Negotiator Config* is used to configure the technical link to the negotiator, for example URL and username. 

The *Negotiator Entity Config* is used to configure which attributes of an entity to use for the interaction with the negotiator.
Optionally a expression can be specified to disable certain rows in the entity for negotiator interaction. If no expression is specified, all rows are enabled for negotiator interaction.

# Interaction between Directory and Negotiator

### Create Query endpoint on Negotiator
The Negotiator has a `create_query` endpoint.
* HTTP method is POST
* Uses HTTP basic authentication with username and password
* Content type is `application/json`
* Response contains a Location header. The Location is a URL on the Negotiator that can be opened in the browser to view
or edit the query.
* Payload of the request is a NegotiatorQuery. This is a JSON message with the following properties:


| Name           | Type   | Explanation |
|----------------|--------|-------------|
| URL*           | String | A callback URL on the Directory where the user can view or update this particular selection of Collections |
| humanReadable* | String | Human readable form of the query used to select the collections |
| collections*   | Array of Collections (`{collectionID, biobankID}`) | The selected collections plus their biobanks. Only the identifiers are sent, the details are available through the standard MOLGENIS REST api on the Directory |
| nToken         | String | Used if an nToken parameter was previously appended to the callback URL on the Directory, so that the Negotiator can recognize which existing query is being updated |

Sample NegotiatorQuery:

```
{
  "URL"           : "https://molgenis…",
  "humanReadable" : "materials is Plasma and Cryo tissue or materials is Not available
sample access fee is required",
  "collections"   : [{
    "collectionID" : "bbmri-eric:collectionID:BE_B0383_LTC",
    "biobankID"    : "bbmri-eric:biobankID:BE_B0383"
  }, {
    "collectionID" : "bbmri-eric:collectionID:BE_B03843_LTCD",
    "biobankID"    : "bbmri-eric:biobankID:BE_B0383"
  }]
}
```
Sequence diagram illustrating the use case where User goes to the Directory to update the biobank collection selection of an existing Negotiator query:
![image](https://www.websequencediagrams.com/cgi-bin/cdraw?lz=VXNlciAtPiBEaXJlY3RvcnkgIDogR0VUKAAHClVSTCArIG5Ub2tlbikKCmFjdGl2YXRlACkKCgoANAotPiBOZWdvdGlhdG9yIDogUE9TAD0PLCBodW1hblJlYWRhYmxlLCBjb2xsZWN0aW9ucywAYAc9AFwSAEwKCgoAVwstAIEvDToAcQxVUkwgKDIwMikKZGUAMhUAgSsLLT4gAIIFBTogKDMwMikgUmVkAIIMBSB0bwBuDAA-CwCBcwsAgj4GAIFrDwoK&s=rose)

## Callback endpoint on Directory
The callback URL to the Directory is provided in the `NegotiatorQuery`.
It can be used to edit the biobank collection selection. The Negotiator must append an `nToken` parameter to the URL.
When the user has updated the query, this `nToken` parameter will be added to the `NegotiatorQuery`.