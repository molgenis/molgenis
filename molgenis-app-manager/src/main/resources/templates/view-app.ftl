<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>
<#assign version = 2>

<#if app.includeMenuAndFooter>
    <@header css js version/>
</#if>

<script>
    // Add user provided app configuration to initial state
    window.__INITIAL_STATE__ = JSON.parse('${app.appConfig}')

    // Add system information like current language to initial state
    window.__INITIAL_STATE__.baseUrl = '${baseUrl}'
    window.__INITIAL_STATE__.lng = '${lng}'
    window.__INITIAL_STATE__.fallbackLng = '${fallbackLng}'
</script>

<div class="container-fluid">
    <div class="row">
        <div class="col-12">
       ${template}
        </div>
    </div>
</div>

<#if app.includeMenuAndFooter>
    <@footer version/>
</#if>