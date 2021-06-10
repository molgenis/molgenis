<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
<#assign version = 2>

<@header css js version/>

<div id="data-row-edit-plugin"></div>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}'<#if dataExplorerBaseUrl??>,
        dataExplorerBaseUrl: '${dataExplorerBaseUrl}'</#if>
    }
</script>

<link rel="stylesheet" href="/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css"/>
<script type=text/javascript src="/@molgenis/expressions"></script>
<script type=text/javascript src="/@molgenis/molgenis-ui-form"></script>
<link rel="stylesheet" href="/@molgenis-ui/data-row-edit/dist/css/app.css"/>
<script type=text/javascript src="/@molgenis-ui/data-row-edit/dist/js/app.js"></script>

<@footer version/>
