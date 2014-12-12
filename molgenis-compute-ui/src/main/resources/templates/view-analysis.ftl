<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["jquery-ui-1.9.2.custom.min.css", "analysis.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "jquery.bootstrap.pager.js", "analysis.js"]>

<@header css js/>
<div class="row">
	<div class="col-md-offset-3 col-md-6">
		<div id="analysis-table-container"></div>
		<form class="form-horizontal" role="form" action="${context_url?html}/create" method="GET">	
			<button class="btn btn-default" id="create-analysis-btn" type="submit">Create analysis</button>
		</form>
	</div>
</div>
<@footer/>