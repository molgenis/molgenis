<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-plugins/jquery.dataTables.css", "TableTools.css", "jquery-plugins/jquery.dataTables_demo_page.css"]>
<#assign js=["jquery-plugins/jquery.dataTables.js", "TableTools.min.js", "ZeroClipboard.js"]>
<@header css js/>
	<div class="row-fluid">${table}</div>
<@footer/>
