<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["data-row-edit/app.css"]>
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

<script type=text/javascript src="<@resource_href "/js/data-row-edit/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/data-row-edit/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/data-row-edit/app.js"/>"></script>

<@footer version/>