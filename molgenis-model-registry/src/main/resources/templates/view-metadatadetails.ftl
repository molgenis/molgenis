<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#import "metadata-macros.ftl" as m>

<#assign css=['dataexplorer.css', 'metadataexplorer.css', 'jquery.molgenis.table.css']>
<#assign js=['jquery-ui-1.9.2.custom.min.js','jquery.bootstrap.pager.js', 'jquery.molgenis.table.js', 'dataexplorer.js', 'dataexplorer-data.js', 'metadatadetails.js']>

<@header css js/>

	<ul class="breadcrumb">
		<li><a href="/menu/${menu_id}">home</a> <span class="divider">/</span></li>
		<li><a href="/menu/${menu_id}/models">models</a> <span class="divider">/</span></li>
		<li class="active">${entityClass.entityClassIdentifier?html}</li>
	</ul>
	
	<@m.renderEntityClassInfo entityClass false />
	
	<div class="well">
		<h3>Attributes</h3>
		<div id="data-table-container"></div>
	</div>
	
	<script>var entityClassIdentifier='${entityClass.entityClassIdentifier?js_string}';</script>
<@footer/>