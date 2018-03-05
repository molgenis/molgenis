<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["questionnaires/app.css"]>
<#assign version = 2>

<@header css js version/>

<div id="questionnaire-app"></div>

<script type="text/javascript">
    window.QUESTIONNAIRE_STATE = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}'
    }
</script>

<script type=text/javascript src="<@resource_href "/js/questionnaires/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/questionnaires/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/questionnaires/app.js"/>"></script>

<@footer version/>