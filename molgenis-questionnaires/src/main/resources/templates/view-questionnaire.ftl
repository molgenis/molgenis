<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["questionnaire/app.css"]>
<#assign version = 2>

<@header css js version/>

<div id="app"></div>

<script type="text/javascript">
    window.QUESTIONNAIRE_STATE = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}',
        questionnaires: ${questionnaires}
    }
</script>

<script type=text/javascript src="<@resource_href "/js/questionnaire/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/questionnaire/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/questionnaire/app.js"/>"></script>

<@footer version/>