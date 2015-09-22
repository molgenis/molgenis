<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[
	"jquery.bootstrap.wizard.css",
	"biobankrefresh.css"]>
<#assign js=[
	"jquery.bootstrap.wizard.min.js",
	"biobankrefresh.js"]>
<@header css js/>
<div class="row">
	<div class="col-md-10 col-md-offset-1 well">
		<legend>Refresh Biobanks Metadata</legend>
		
		<div class="row">
			<div id="button-holder" class="col-md-6">
			</div>
		</div>
	</div>
</div>
<@footer/>
