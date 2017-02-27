## Third-party software
For some modules in Molgenis, third-party software is in use. It is important to know that some of these licenses are different than the Molgenis license.

In this section you can find a list of remarks about third-party software in Molgenis modules.

### molgenis-charts module
As a non-profit organisation we are using the Highsoft software 'highstock version 1.3.6', in the molgenis-charts module to build some charts.

Important! The Highsoft software product is not free for commercial use. For Highsoft products and pricing go to: http://shop.highsoft.com/

To turn-off/deactivate this functionality you can set the RuntimeProperty “plugin.dataexplorer.mod.charts” to false:
    1. You can find the RuntimeProperty via the menu Entities -> RuntimeProperty.
    2. Search for the RuntimeProperty "plugin.dataexplorer.mod.charts" and update to false.
    3. If it does not exists:
        a. Create a new RuntimeProperty
        b. Name -> plugin.dataexplorer.mod.charts
        c. Value -> false