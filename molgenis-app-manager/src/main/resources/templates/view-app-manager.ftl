<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css = []>
<#assign js=[]>
<#assign version = 2>

<@header css js version/>
    <div id="app-manager"></div>

    <link rel="stylesheet" href="/@molgenis-ui/app-manager/dist/css/chunk-vendors.css"/>
    <link rel="stylesheet" href="/@molgenis-ui/app-manager/dist/css/app.css"/>

    <script type=text/javascript src="/@molgenis-ui/app-manager/dist/js/chunk-vendors.js"></script>
    <script type=text/javascript src="/@molgenis-ui/app-manager/dist/js/app.js"></script>

<@footer version/>
