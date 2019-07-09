<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
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

<link rel="stylesheet" href="/ui/@molgenis-ui/one-click-importer/dist/css/app.css"/>

<script type=text/javascript src="/ui/@molgenis-ui/one-click-importer/dist/js/manifest.js"></script>
<script type=text/javascript src="/ui/@molgenis-ui/one-click-importer/dist/js/vendor.js"></script>
<script type=text/javascript src="/ui/@molgenis-ui/one-click-importer/dist/js/app.js"></script>

<@footer version/>
