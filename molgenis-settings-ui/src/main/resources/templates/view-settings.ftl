<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["settings/app.css"]>
<#assign version = 2>

<@header css js version/>

<div id="settings-plugin"></div>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}',
        isSuperUser: ${isSuperUser?c}
    }
</script>

<script type=text/javascript src="<@resource_href "/js/settings/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/settings/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/settings/app.js"/>"></script>

<@footer version/>