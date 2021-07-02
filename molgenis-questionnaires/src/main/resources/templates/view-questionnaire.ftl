<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
<#assign jsGlobal = ["/@molgenis/expressions", "/@molgenis/molgenis-ui-form"]>
<#assign version = 2>

<@header css js version jsGlobal/>

<div id="questionnaire-app"></div>

<script type="text/javascript">
    window.QUESTIONNAIRE_STATE = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}',
        username: '${username}'
    }
</script>

<link rel="stylesheet" href="/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css"/>
<link rel="stylesheet" href="/@molgenis-ui/questionnaires/dist/css/app.css"/>

<script type=text/javascript src="/@molgenis-ui/questionnaires/dist/js/manifest.js"></script>
<script type=text/javascript src="/@molgenis-ui/questionnaires/dist/js/vendor.js"></script>
<script type=text/javascript src="/@molgenis-ui/questionnaires/dist/js/app.js"></script>

<@footer version/>
