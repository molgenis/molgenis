<#macro annotation_header>

	<#include "molgenis-header.ftl">
	
	<#assign css=["annotate-ui.css", "jquery-ui-1.10.3.custom.css", "chosen.css", "bootstrap-fileupload.css"]>
	<#assign js=["annotation.ui.js", "jquery-ui-1.10.3.custom.min.js", "chosen.jquery.min.js", "bootstrap-fileupload.js", "jquery.bootstrap.wizard.js"]>
		
	<@header css js />

</#macro>

<#macro annotation_footer>
	<#include "molgenis-footer.ftl">
	<@footer />
</#macro>