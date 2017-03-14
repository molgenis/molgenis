[![Build Status](https://molgenis50.gcc.rug.nl/jenkins/buildStatus/icon?job=molgenis)](http://www.molgenis.org/jenkins/job/molgenis/)

# Welcome to MOLGENIS

MOLGENIS is a collaborative open source project on a mission to generate great software infrastructure for life science research. 

See http://molgenis.github.io for documentation.

## Installation
See [Getting Started Guide](docs/develop/start.md).

## Third-party software
For some modules in MOLGENIS, third-party software is in use. It is important to know that some of these licenses are different than the MOLGENIS license.
	
In this section you can find a list of remarks about third-party software in MOLGENIS modules.		
	
### molgenis-charts module		
As a non-profit organisation we are using the Highsoft software 'highstock version 1.3.6', in the molgenis-charts module to build some charts.		
		
Important! The Highsoft software product is not free for commercial use. For Highsoft products and pricing go to: http://shop.highsoft.com/		
		
To turn-off/deactivate this functionality you can change the relevant setting for the data explorer:  		
1. In the menu select Admin -> Settings		
2. Select the dataexplorer settings		
3. Set the value for Charts to `No`

# Documentation
{% include "./docs/SUMMARY.md" %}
