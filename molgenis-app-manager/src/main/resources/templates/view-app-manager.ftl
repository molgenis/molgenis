<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css = []>
<#assign js=[]>
<#assign version = 2>
<@header css js version/>

<div id="app-manager"></div>

<link rel="stylesheet" href="/ui/@molgenis-ui/app-manager/dist/css/app.css"/>

<script type=text/javascript src="/ui/@molgenis-ui/app-manager/dist/js/manifest.js"></script>
<script type=text/javascript src="/ui/@molgenis-ui/app-manager/dist/js/vendor.js"></script>
<script type=text/javascript src="/ui/@molgenis-ui/app-manager/dist/js/app.js"></script>

<@footer version/>
