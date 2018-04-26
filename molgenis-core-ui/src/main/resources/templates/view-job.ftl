<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["job.js"]>

<@header css js/>

<div class="row">
    <div class="col-md-12">
        <div id="job-container"
             data-href="${jobHref}"
             data-timeout="${refreshTimeoutMillis?c}"></div>
    </div>
</div>

<@footer/>