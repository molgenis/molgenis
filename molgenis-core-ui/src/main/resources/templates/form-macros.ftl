<#macro renderList form index=0>
<div id="list-holder" class="row-fluid">
	<div id="list-navigation">
		<div class="span4">
			<h3 class="pull-left">${form.title?html} (<span id="entity-count-${index}"></span>)</h3>
			<#if form.hasWritePermission>
				<a id="create-${index}" style="margin:30px 10px" class="pull-left" href="${form.getBaseUri(context_url)?html}/create?back=${current_uri?url('UTF-8')?html}">
					<img src="/img/new.png" />
				</a>
			</#if>
		</div>
				
		<div class="data-table-pager-container span4">
			<div id="data-table-pager-${index}" class="pagination pagination-centered"></div>
		</div>
		
		<#if index==0>
			<form id="search-form" class="form-search text-center pull-right" method="GET" action="#">
				<select id="query-fields">
					<#list form.metaData.fields as field>
						<#if field.dataType.enumType == 'STRING'>
							<option id="${field.name?html}">${field.name?html}</option>
						</#if>
					</#list>
				</select>
				<select id="operators">
					<option id="EQUALS">EQUALS</option>
					<option id="NOT">NOT EQUALS</option>
					<option id="LIKE">LIKE</option>
				</select>
				<div class="input-append">
    				<input type="search" class="span8 search-query" name="q" placeholder="SEARCH">
    				<button type="submit" class="btn"><i class="icon-search icon-large"></i> </button>
  				</div>
			</form>
		</#if>				
	</div>
	
	<div class="form-list-holder">
		<table class="table table-striped table-bordered table-hover table-condensed">
			<thead>
				<tr>
					<th class="edit-icon-holder">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th><#-- Non breaking spaces are for fixing very annoying display error in chrome -->
					<#if form.hasWritePermission>
						<th class="edit-icon-holder">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
					</#if>
					<#list form.metaData.fields as field>
						<th>${field.name?html}</th>
					</#list>
				</tr>
			</thead>
			<tbody id="entity-table-body-${index}">
			</tbody>
		</table>
	</div>
	
</div>
</#macro>

<#macro meta form index=0>
<script>
	forms[${index}] = {
		title: '${form.title?html}',
		hasWritePermission: ${form.getHasWritePermission()?string},
		<#if form.primaryKey??>
			primaryKey: '${form.primaryKey?html}',
		</#if>
		<#if form.xrefFieldName??>
			xrefFieldName: '${form.xrefFieldName?html}',
		</#if>
		baseUri: '${form.getBaseUri(context_url)?html}'
	}
	
	//Build metadata to be used by the js
	forms[${index}].meta = {
		name:'${form.metaData.name?lower_case?html}'
	}
	
	//The fieldnames of the entity
	forms[${index}].meta.fields = [<#list form.metaData.fields as field>
					{
						name:'${field.name?html}', 
						xref:${(field.dataType.enumType == 'XREF' || field.dataType.enumType == 'CATEGORICAL')?string('true', 'false')},
						mref:${(field.dataType.enumType == 'MREF')?string('true', 'false')},
						type:'${field.dataType.enumType?html}',
						readOnly:${field.isReadonly()?string('true', 'false')},
						unique:false,
						<#if field.refEntity??>
							<#if field.refEntity.labelAttribute??>
								xrefLabelName: '${field.refEntity.labelAttribute.name?html}',
							</#if>
						xrefLabel: '${field.refEntity.name?html}',
						xrefEntityName: '${field.refEntity.name?lower_case?html}'
						</#if>
					}
					<#if field_has_next>,</#if>
				</#list>];
				
	//Get the label attribute
	<#if form.metaData.labelAttribute??>
		forms[${index}].meta.labelFieldName = '${form.metaData.labelAttribute.name?html}';
	</#if>
	
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
				${field.name?html}: {
					remote: {
						url: '/api/v1/${form.metaData.name?lower_case?html}?q[0].operator=EQUALS&q[0].field=${field.name?html}',
						async: false,
						data: {
							'q[0].value': function() {//Bit cheesy, but it works, is appended to the url
								return $('#${field.name?html}').val();
							}
						},
						dataFilter: function(data) {
							var apiResponse = JSON.parse(data);
								
							if (apiResponse.total == 0) {
								return 'true';
							}
								
							<#if form.primaryKey??>
							if (apiResponse.items[0].href.endsWith('<#if form.primaryKey?is_number>${form.primaryKey?c}<#else>${form.primaryKey}</#if>')) {
								return 'true'; //Update
							}
							</#if>
								
							return 'false';
						}
					},
					<#if field.range??>
						range: [${field.range.min?c},${field.range.max?c}]
					</#if>
				},
			<#elseif field.range??>
				${field.name}: {
					range: [${field.range.min?c},${field.range.max?c}]
				},
			</#if>
		</#list>
	};
	
	var remoteMessages = {
		<#list form.metaData.fields as field>
			<#if field.isUnique()?string('true', 'false') == 'true'>
				${field.name?html}: {
					remote: "This ${field.name?html} already exists. It must be unique"
				},
			</#if>
		</#list>
	};
</script>
</#macro>

