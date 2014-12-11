<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>
<#if message??>
	<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>SUCCESS!</strong> ${message?html}</div>	
</#if>
<h1>Workflow id: ${workflowId!"not specified"?html}</h1>
<h1>Target id: ${targetId!"not specified"?html}</h1>
<h1>Query: ${q!"not specified"?html}</h1>
<form name="execute-workflow-form" class="form-horizontal" action="${context_url?html}/execute" method="POST">
	<input type="hidden" name="workflowId" value="${workflowId?html}">
	<input type="hidden" name="targetId" value="${targetId?html}">
	<button type="submit" class="btn btn-default">Run</button>	
</form>
<@footer/>