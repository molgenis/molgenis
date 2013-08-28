<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=['molgenis-form-list.js']>

<@header css js/>

<div class="row-fluid">
	<h3>${form.title}</h3>
		
	<table class="table table-striped">
		<thead>
			<tr>
				<#list form.metaData.fields as field>
					<th>${field.label}</th>
				</#list>
			</tr>
		</thead>
		<tbody id="entity-table-body">
		</tbody>
	</table>
		
	<div class="row-fluid data-table-pager-container">
		<div id="data-table-pager" class="pagination pagination-centered"></div>
	</div>	
</div>

<script>
	//Build metadata to be used by the js
	var meta = {name:'${form.metaData.name}'};
	
	//The fieldnames of the entity
	meta.fields = [<#list form.metaData.fields as field>
					{
						name:'${field.name?uncap_first}', 
						xref:${field.isXRef()?string},
						mref:${field.isMRef()?string},
						
						<#if field.isXRef()?string == 'true' || field.isMRef()?string == 'true'>
							xrefLabelName: '${field.xrefLabelNames[0]?uncap_first}'
						</#if>
					}
					<#if field_has_next>,</#if>
				</#list>];
</script>
	
<@footer/>