<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
<#assign version = 2>
<#assign jsGlobal=['/@molgenis/expressions', '/@molgenis/molgenis-ui-form']>

<@header css js version jsGlobal/>

<div id="settings-plugin"></div>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}',
        isSuperUser: ${isSuperUser?c}
    }
</script>
<link rel="stylesheet" href="/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css"/>
<link rel="stylesheet" href="/@molgenis-ui/settings/dist/css/app.css"/>
<script type=text/javascript src="/@molgenis-ui/settings/dist/js/app.js"></script>

<@footer version/>
