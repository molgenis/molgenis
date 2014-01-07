<#macro renderList form index=0>
<div id="list-holder" class="row-fluid">
	<div id="list-navigation">
		<div class="span4">
			<h3 class="pull-left">${form.title} (<span id="entity-count-${index}"></span>)</h3>
			<#if form.hasWritePermission>
				<a id="create-${index}" style="margin:30px 10px" class="pull-left" href="${form.getBaseUri(context_url)}/create?back=${current_uri?url('UTF-8')}">
					<img src="/img/new.png" />
				</a>
			</#if>
		</div>
				
		<div class="data-table-pager-container span4">
			<div id="data-table-pager-${index}" class="pagination pagination-centered"></div>
		</div>
		
		<#if index==0>
			<form class="form-search text-center pull-right" method="get" action="#">
				<select id="query-fields">
					<#list form.metaData.fields as field>
						<#if field.dataType.enumType == 'STRING'>
							<option id="${field.name}">${field.name}</option>
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
						<th>${field.name}</th>
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
						xref:${(field.dataType.enumType == 'XREF')?string('true', 'false')},
						mref:${(field.dataType.enumType == 'MREF')?string('true', 'false')},
						type:'${field.dataType.enumType}',
						readOnly:${field.isReadonly()?string('true', 'false')},
						unique:false,
						<#if field.refEntity??>
						xrefLabelName: '${field.refEntity.labelAttribute.name?uncap_first}',
						xrefLabel: '${field.refEntity.name}',
						xrefEntityName: '${field.refEntity.name?lower_case}'
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
		<#-- TODO
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
		-->
	};
	
	var remoteMessages = {
		<#-- TODO
		<#list form.metaData.fields as field>
			<#if field.isUnique()?string('true', 'false') == 'true'>
				${field.name?uncap_first}: {
					remote: "This ${field.name?uncap_first} already exists. It must be unique"
				},
			</#if>
		</#list>
		-->
	};
</script>
</#macro>

