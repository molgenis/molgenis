<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["gaflistimporter.js"]>
<@header css js/>
	<form id="gaflist-import-form" method="post" action="${context_url}/import">
		<div class="row-fluid">
			<button type="submit" class="btn">Import GAF list</button>
		</div>
	</form>
<@footer/>
