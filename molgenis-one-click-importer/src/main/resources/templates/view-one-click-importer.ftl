<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["molgenis-one-click-importer/app.css"]>
<#assign version = 2>

<@header css js version/>

<div id="app"></div>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}'<#if navigatorBaseUrl??>,
        isSuperUser: ${isSuperUser?c},
        navigatorBaseUrl: '${navigatorBaseUrl}'</#if><#if dataExplorerBaseUrl??>,
        dataExplorerBaseUrl: '${dataExplorerBaseUrl}'</#if>
    }
</script>

<script type=text/javascript src="<@resource_href "/js/molgenis-one-click-importer/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/molgenis-one-click-importer/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/molgenis-one-click-importer/app.js"/>"></script>

<@footer version/>