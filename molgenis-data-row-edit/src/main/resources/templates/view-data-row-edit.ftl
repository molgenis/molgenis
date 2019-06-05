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
        fallbackLng: '${fallbackLng}',
        dataExplorerBaseUrl: '${dataExplorerBaseUrl}'
    }
</script>

<link rel="stylesheet" href="/@molgenis-ui/data-row-edit/dist/css/app.css"/>

<script type=text/javascript src="<@resource_href "/@molgenis-ui/data-row-edit/dist/js/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/@molgenis-ui/data-row-edit/dist/js/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/@molgenis-ui/data-row-edit/dist/js/app.js"/>"></script>

<@footer version/>
