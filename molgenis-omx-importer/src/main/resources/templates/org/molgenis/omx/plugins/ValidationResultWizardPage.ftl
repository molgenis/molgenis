<div>
<#if wizard.dataImportable??>
	<table class="table table-bordered table-condensed pull-left" style="width: 25%;">
		<thead>
			<tr><th colspan="2" style="text-align: center;"><h4>Data</h4></th></tr>
			<tr>
				<th>Name</th>
				<th>Importable</th>
			</tr>
		</thead>
		<tbody>
			<#list wizard.dataImportable?keys as name>
				<tr>
					<td>${name}</td>
					<#if wizard.dataImportable[name] == true>
						<td class="alert alert-success" style="text-align: center;">Yes</td>
					<#else>
						<td class="alert alert-error" style="text-align: center;">No</td>
					</#if>
				</tr>
			</#list>
		</tbody>
	</table>
</#if>

<#if wizard.entitiesImportable??>

<table class="table table-bordered table-condensed pull-left" style="width: 25%;margin-left:50px">
	<thead>
		<tr><th colspan="2" style="text-align: center;"><h4>Entities</h4></th></tr>
		<tr>
			<th>Name</th>
			<th>Importable</th>
		</tr>
	</thead>
	<tbody>
	<#list wizard.entitiesImportable?keys as entity>
		<tr>
			<td>
				<#if wizard.entitiesImportable[entity] == true>
					<a href="generated-doc/fileformat.html#${entity}_entity" target="_blank">${entity}</a>
				<#else>
					${entity}
				</#if>
			</td>
			
			<#if wizard.entitiesImportable[entity] == true>
				<td class="alert alert-success" style="text-align: center;">Yes</td>
			<#else>
				<td class="alert alert-error" style="text-align: center;">No</td>
			</#if>
			
		</tr>
	</#list>
	</tbody>
</table>
<div class="clearfix"></div>
</div>

<table class="table table-bordered table-condensed">
	<thead>
		<tr><th colspan="5" style="text-align: center;"><h4>Entity fields</h4></th></tr>
		<tr>
			<th>Name</th>
			<th>Detected</th>
			<th>Required</th>
			<th>Available</th>
			<th>Unknown</th>
		</tr>
	</thead>
	
	<#list wizard.entitiesImportable?keys as entity>
		<#if wizard.entitiesImportable[entity] == true>
			<tr>
				<td>
					<a href="generated-doc/fileformat.html#${entity}_entity">${entity}</a>
				</td>
				<#if wizard.fieldsDetected[entity]?size gt 0>
					<td class="alert alert-success">
						<#list wizard.fieldsDetected[entity] as field>
							${field}<#if field_has_next>, </#if>
						</#list>
					</td>
				<#else>
					<td class="alert alert-error">No fields detected</td>
				</#if>
				
				<#if wizard.fieldsRequired[entity]?size gt 0>
					<td class="alert alert-error">
						<#list wizard.fieldsRequired[entity] as field>
							${field}<#if field_has_next>, </#if>
						</#list>
					</td>
				<#else>
					<td class="alert alert-success">No missing fields</td>
				</#if>
			
				
				<#if wizard.fieldsAvailable[entity]?size gt 0>
					<td class="alert alert-info">
						<#list wizard.fieldsAvailable[entity] as field>
							${field}<#if field_has_next>, </#if>
						</#list>
					</td>
				<#else>
					<td class="alert alert-success">No optional fields</td>
				</#if>
				
				
				<#if wizard.fieldsUnknown[entity]?size gt 0>
					<td class="alert alert-warning">
						<#list wizard.fieldsUnknown[entity] as field>
							${field}<#if field_has_next>, </#if>
						</#list>
					</td>
				<#else>
					<td class="alert alert-success">No unknown fields</td>
				</#if>
				
			</tr>
		</#if>
	</#list>
</table>
</#if>
