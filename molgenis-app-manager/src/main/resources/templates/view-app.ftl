<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>
<#assign version = 2>

<#if app.includeMenuAndFooter>
    <@header css js version/>
</#if>

<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            ${app.templateContent}
            <script>
                window.__INITIAL_STATE__ = JSON.parse('${app.appConfig}')
            </script>
        </div>
    </div>
</div>

<#if app.includeMenuAndFooter>
    <@footer/>
</#if>