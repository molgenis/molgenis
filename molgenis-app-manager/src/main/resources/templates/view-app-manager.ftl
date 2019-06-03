<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css = []>
<#assign js=[]>
<#assign version = 2>
<@header css js version/>

<div id="app-manager"></div>

<link rel="stylesheet" href="/@molgenis-ui/app-manager/dist/css/app-manager/app.css"/>

<script type=text/javascript src="<@resource_href "/@molgenis-ui/app-manager/dist/js/app-manager/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/@molgenis-ui/app-manager/dist/js/app-manager/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/@molgenis-ui/app-manager/dist/js/app-manager/app.js"/>"></script>

<@footer version/>
