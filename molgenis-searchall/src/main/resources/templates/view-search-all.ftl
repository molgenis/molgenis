<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["searchall/app.css"]>
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

<script type=text/javascript src="<@resource_href "/js/searchall/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/searchall/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/searchall/app.js"/>"></script>

<@footer version/>