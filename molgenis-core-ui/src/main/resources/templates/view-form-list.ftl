<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=['molgenis-form.css']>
<#assign js=['molgenis-form-list.js']>

<@header css js/>

<div class="row-fluid">
	<div class="row-fluid">
		<div class="span4" style="height: 80px">
			<h2>${form.title} (<span id="entity-count"></span>)</h2>
		</div>
				
		<div class="data-table-pager-container span4" style="height: 80px">
			<div id="data-table-pager" class="pagination pagination-centered"></div>
		</div>
		
		<div class="span4" style="height: 78px;">
			<div class="pull-right" style="vertical-align: bottom; height:78px;line-height:78px"><a href="${context_url}/${form.metaData.name}/create"><img src="/img/new.png" /></a></div>
		</div>
	</div>
			
	<div id="success-message" class="control-group" style="display: none">
    	<div class="controls">
			<div class="alert alert-success">
  				<button type="button" class="close">&times;</button>
  				<strong>${form.title} deleted.</strong>
			</div>
		</div>
	</div>
		
	<div id="error-message" class="control-group" style="display: none">
    	<div class="controls">
			<div class="alert alert-error">
  				<button type="button" class="close">&times;</button>
  				<strong>Could not delete ${form.title}</strong>
			</div>
		</div>
	</div>
		
	<table class="table table-striped table-bordered table-hover">
		<thead>
			<tr>
				<#if form.hasWritePermission>
					<th class="edit-icon-holder">&nbsp;</th>
					<th class="edit-icon-holder">&nbsp;</th>
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