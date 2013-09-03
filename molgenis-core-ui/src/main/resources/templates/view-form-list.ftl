<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=['molgenis-form.css']>
<#assign js=['molgenis-form-list.js']>

<@header css js/>

<div class="row-fluid">
	<div class="row-fluid">
		<div class="span4">
			<h2>${form.title} (<span id="entity-count"></span>)</h2>
		</div>
				
		<div class="data-table-pager-container span4">
			<div id="data-table-pager" class="pagination pagination-centered"></div>
		</div>
	</div>
			
	<table class="table table-striped table-bordered<#if form.hasWritePermission> table-hover</#if>">
		<thead>
			<tr>
				<#if form.hasWritePermission>
					<th id="edit-icon-holder">&nbsp;</th>
				</#if>
				<#list form.metaData.fields as field>
					<th>${field.label}</th>
				</#list>
			</tr>
		</thead>
		<tbody id="entity-table-body">
		</tbody>
	</table>
	
</div>

<#include "view-form-meta.ftl">
	
<@footer/>