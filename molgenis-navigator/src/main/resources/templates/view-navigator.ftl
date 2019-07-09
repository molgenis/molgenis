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
        fallbackLng: '${fallbackLng}',
        isSuperUser: ${isSuperUser?c},
        pluginUrls: {
            'dataexplorer': <#if dataexplorer??>'${dataexplorer}'<#else>null</#if>,
            'importwizard': <#if importwizard??>'${importwizard}'<#else>null</#if>,
            'metadata-manager': <#if metadata_manager??>'${metadata_manager}'<#else>null</#if>,
        }
    }
</script>

<link rel="stylesheet" href="/ui/@molgenis-ui/navigator/dist/css/app.css"/>

<script type=text/javascript src="/ui/@molgenis-ui/navigator/dist/js/manifest.js"></script>
<script type=text/javascript src="/ui/@molgenis-ui/navigator/dist/js/vendor.js"></script>
<script type=text/javascript src="/ui/@molgenis-ui/navigator/dist/js/app.js"></script>

<@footer version/>
