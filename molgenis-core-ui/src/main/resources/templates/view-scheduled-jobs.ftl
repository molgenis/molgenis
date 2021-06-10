<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=['scheduled-jobs.js']>

<@header css js/>

<link rel="stylesheet" href="/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css"/>
<script type=text/javascript src="/@molgenis/expressions"></script>
<script type=text/javascript src="/@molgenis/molgenis-ui-form"></script>

<div class="row">
    <div class="col-md-12">
        <div id="scheduled-jobs-plugin"></div>
    </div>
</div>

<@footer/>