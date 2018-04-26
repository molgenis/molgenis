<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css = ["app-manager/app.css"]>
<#assign js=[]>
<#assign version = 2>
<@header css js version/>

<div id="app-manager"></div>

<script type=text/javascript src="<@resource_href "/js/app-manager/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/app-manager/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/app-manager/app.js"/>"></script>

<@footer/>
