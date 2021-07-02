<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=['scheduled-jobs.js']>
<#assign version = 1>
<#assign jsGlobal=['/@molgenis/expressions']>

<@header css js version jsGlobal/>

<div class="row">
    <div class="col-md-12">
        <div id="scheduled-jobs-plugin"></div>
    </div>
</div>

<@footer/>