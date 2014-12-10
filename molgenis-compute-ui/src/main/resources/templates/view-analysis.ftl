<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>
<h1>Workflow id: ${workflowId!"not specified"}</h1>
<h1>Target id: ${targetId!"not specified"}</h1>
<h1>Query: ${q!"not specified"}</h1>
<@footer/>