<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["navigator/app.css"]>
<#assign version = 2>
<@header css js version/>

<div id="app"></div>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}',
        isSuperUser: ${isSuperUser?c}
    }
</script>

<script type=text/javascript src="<@resource_href "/js/navigator/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/navigator/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/navigator/app.js"/>"></script>

<@footer version/>