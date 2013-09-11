<#macro renderList form index=0>
<div class="row-fluid">
	<div class="row-fluid">
		<div class="span4" style="height: 80px">
			<h2>${form.title} (<span id="entity-count-${index}"></span>)</h2>
		</div>
				
		<div class="data-table-pager-container span4" style="height: 80px">
			<div id="data-table-pager-${index}" class="pagination pagination-centered"></div>
		</div>
		
		<div class="span4" style="height: 78px;">
			<#if form.hasWritePermission>
				<div class="pull-right" style="vertical-align: bottom; height:78px;line-height:78px"><a href="${form.getBaseUri(context_url)}/create"><img src="/img/new.png" /></a></div>
			</#if>
		</div>
	</div>
			
	<table class="table table-striped table-bordered table-hover">
		<thead>
			<tr>
				<th class="edit-icon-holder">&nbsp;</th>
				<#if form.hasWritePermission>
					<th class="edit-icon-holder">&nbsp;</th>
				</#if>
				<#list form.metaData.fields as field>
					<th>${field.label}</th>
				</#list>
			</tr>
		</thead>
		<tbody id="entity-table-body-${index}">
		</tbody>
	</table>
</div>
</#macro>

<#macro meta form index=0>
<script>
	forms[${index}] = {
		title: '${form.title}',
		hasWritePermission: ${form.getHasWritePermission()?string},
		<#if form.primaryKey??>
			primaryKey: '${form.primaryKey}',
		</#if>
		<#if form.xrefFieldName??>
			xrefFieldName: '${form.xrefFieldName?uncap_first}',
		</#if>
		baseUri: '${form.getBaseUri(context_url)}'
	}
	
	//Build metadata to be used by the js
	forms[${index}].meta = {
		name:'${form.metaData.name?lower_case}'
	}
	
	//The fieldnames of the entity
	forms[${index}].meta.fields = [<#list form.metaData.fields as field>
					{
						name:'${field.name?uncap_first}', 
						xref:${field.isXRef()?string('true', 'false')},
						mref:${field.isMRef()?string('true', 'false')},
						type:'${field.type.enumType}',
						readOnly:${field.isReadOnly()?string('true', 'false')},
						unique:${field.isUnique()?string('true', 'false')},
						<#if field.isXRef()?string == 'true' || field.isMRef()?string == 'true'>
							xrefLabelName: '${field.xrefLabelNames[0]?uncap_first}',
							xrefLabel: '${field.xrefEntity.label}',
							xrefEntityName: '${field.xrefEntity.name?lower_case}'
						</#if>
					}
					<#if field_has_next>,</#if>
				</#list>];
			
	//Get a field by name				
	forms[${index}].meta.getField = function(name) {
		for (var i = 0; i < form${index}.meta.fields.length; i++) {
			if (forms[${index}].meta.fields[i].name == name) {
				return forms[${index}].meta.fields[i];
			}
		}
		
		return null;
	}
	
	//Get the name of a xref or mref field by it's
	forms[${index}].meta.getXrefFieldName = function(xrefEntityName) {
		for (var i = 0; i < form${index}.meta.fields.length; i++) {
			if ((forms[${index}].meta.fields[i].xref || forms[${index}].meta.fields[i].mref) 
				&& (forms[${index}].meta.fields[i].xrefEntityName == xrefEntityName)) {
				return forms[${index}].meta.fields[i].name;
			}
		}
		return null;
	}
	
	forms[${index}].meta.getXRefAndMRefFieldNames = function() {
		var fields = [];
		for (var i = 0; i < form${index}.meta.fields.length; i++) {
			if (forms[${index}].meta.fields[i].xref || forms[${index}].meta.fields[i].mref) {
				fields.push(forms[${index}].meta.fields[i].name);
			}
		}
		
		return fields;
	}
</script>
</#macro>

<#macro remoteValidationRules form>
<script>
	//Remote validation rules for unique fields (check if unique)
	var remoteRules = {
		<#list form.metaData.fields as field>
			<#if field.isUnique()?string('true', 'false') == 'true'>
				${field.name?uncap_first}: {
					remote: {
						url: '/api/v1/${form.metaData.name?lower_case}?q[0].operator=EQUALS&q[0].field=${field.name?uncap_first}',
						async: false,
						data: {
							'q[0].value': function() {//Bit cheesy, but it works, is appended to the url
								return $('#${field.name?uncap_first}').val();
							}
						},
						dataFilter: function(data) {
							var apiResponse = JSON.parse(data);
								
							if (apiResponse.total == 0) {
								return 'true';
							}
								
							<#if form.primaryKey??>
							if (apiResponse.items[0].href.endsWith('${form.primaryKey}')) {
								return 'true'; //Update
							}
							</#if>
								
							return 'false';
						}
					}
				},
			</#if>
		</#list>
	};
	
	var remoteMessages = {
		<#list form.metaData.fields as field>
			<#if field.isUnique()?string('true', 'false') == 'true'>
				${field.name?uncap_first}: {
					remote: "This ${field.name?uncap_first} already exists. It must be unique"
				},
			</#if>
		</#list>
	};
</script>
</#macro>

