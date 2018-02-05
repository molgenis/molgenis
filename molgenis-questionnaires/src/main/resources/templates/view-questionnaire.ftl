<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = ["questionnaire/app.css"]>
<#assign version = 2>

<@header css js version/>

<div class="test">
    <#list questionnaires as questionnaire>
        ${questionnaire}
    </#list>
</div>

<div id="app"></div>

<script type="text/javascript">

    var questionnaires = []
    <#list questionnaires as questionnaire>
        questionnaires.push(${questionnaire})
    </#list>

    window.QUESTIONNAIRE_STATE = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}',
        questionnaires: questionnaires
    }
</script>

<script type=text/javascript src="<@resource_href "/js/questionnaire/manifest.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/questionnaire/vendor.js"/>"></script>
<script type=text/javascript src="<@resource_href "/js/questionnaire/app.js"/>"></script>

<@footer version/>