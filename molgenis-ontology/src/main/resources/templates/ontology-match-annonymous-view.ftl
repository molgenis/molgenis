<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#include "ontology-match-new-task.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ontology-service.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js"]>
<@header css js/>
<form id="ontology-match" class="form-horizontal">
	<@ontologyMatchNewTask />
</form>
<@footer/>