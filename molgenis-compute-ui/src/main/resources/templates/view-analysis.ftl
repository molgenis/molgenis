<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["jquery-ui-1.9.2.custom.min.css", "analysis.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "jquery.bootstrap.pager.js", "analysis.js"]>

<@header css js/>
<div class="row">
	<div class="col-md-offset-3 col-md-6">
		<div id="analysis-table-container"></div>
		<button class="btn btn-default" id="create-analysis-btn" type="button">Create analysis</button>
	</div>
</div>
<@footer/>