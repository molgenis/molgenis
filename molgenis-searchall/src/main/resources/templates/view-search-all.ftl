<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
<#assign version = 2>
<@header css js version/>

<div id="app"></div>

<script type="text/javascript">
    window.searchall = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        isSuperUser: ${isSuperUser?c},
        fallbackLng: '${fallbackLng}'<#if navigatorBaseUrl??>,
        navigatorBaseUrl: '${navigatorBaseUrl}'</#if><#if dataExplorerBaseUrl??>,
        dataExplorerBaseUrl: '${dataExplorerBaseUrl}'</#if>
    }
</script>

<link rel="stylesheet" href="/@molgenis-ui/searchall/dist/css/app.css"/>

<script type=text/javascript src="<@resource_href "/@molgenis-ui/searchall/dist/js/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/@molgenis-ui/searchall/dist/js/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/@molgenis-ui/searchall/dist/js/app.js"/>"></script>

<@footer version/>
