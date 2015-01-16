<#macro renderList form index=0>
	<div class="row">
		<div class="col-md-4">		
			<h3 class="pull-left">
				${form.title?html} (<span id="entity-count-${index?html}"></span>)
			</h3>
			
			<#if form.hasWritePermission>
				<a id="create-${index}" style="margin:20px 10px" class="pull-left" href="${form.getBaseUri(context_url)?html}/create?back=${current_uri?html?url('UTF-8')}">
					<img src="/img/new.png" />
				</a>
			</#if>
		</div>			
			
		<div class="col-md-4 data-table-pager-container">
			<div id="data-table-pager-${index}"></div>
		</div>
		
		<div class="col-md-4">
			<#if index==0>
				<form class="form-search form-inline text-center pull-right" method="get" action="#">
	                <div class="form-group">
	    				<select class="form-control" id="query-fields">
                                    <option value="" selected></option>
	    					<#list form.metaData.fields as field>
	    							<option id="${field.name?html}">${field.name?html}</option>
	    					</#list>
	    				</select>
					</div>
					
					<div class="form-group">
	    				<select class="form-control" id="operators">
                            <option id="SEARCH">SEARCH</option>
	    					<option id="EQUALS">EQUALS</option>
	    					<option id="LIKE">LIKE</option>
	    				</select>
					</div>
					
					<div class="form-group">
						<div class="col-md-8">	
	    					<div class="input-group">
	    				    	<input type="search" class="search-query form-control" name="q" placeholder="SEARCH">
		                        <span class="input-group-btn">
		                            <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-search"></span></button>
		                        </span>
	                        </div>
	                    </div>
	                </div>  
				</form>
			</#if>
		</div>			
	</div>
		
	<div class="row">
		<div class="col-md-12">
			<div id="entity-table-container">		
				<table class="table table-striped table-bordered table-hover table-condensed">
					<thead>
						<tr>
							<th class="edit-icon-holder"></th>
							<#if form.hasWritePermission>
								<th class="edit-icon-holder"></th>
							</#if>
							<#list form.metaData.fields as field>
								<th>${field.name?html}</th>
							</#list>
						</tr>
					</thead>
					<tbody id="entity-table-body-${index}"></tbody>
				</table>
			</div>
		</div>
	</div>
</#macro>

<#macro meta form index=0>
<script>
	forms[${index}] = {
		title: '${form.title?js_string}',
		hasWritePermission: ${form.getHasWritePermission()?string},
		<#if form.primaryKey??>
			primaryKey: '${form.primaryKey?js_string}',
		</#if>
		<#if form.xrefFieldName??>
			xrefFieldName: '${form.xrefFieldName?js_string}',
		</#if>
		baseUri: '${form.getBaseUri(context_url)?js_string}'
	}
	
	//Build metadata to be used by the js
	forms[${index}].meta = {
		name:'${form.metaData.name?lower_case?js_string}'
	}
	
	//The fieldnames of the entity
	forms[${index}].meta.fields = [<#list form.metaData.fields as field>
					{
						name:'${field.name?js_string}', 
						xref:${(field.dataType.enumType == 'XREF' || field.dataType.enumType == 'CATEGORICAL')?string('true', 'false')},
						mref:${(field.dataType.enumType == 'MREF')?string('true', 'false')},
						type:'${field.dataType.enumType?js_string}',
						readOnly:${field.isReadonly()?string('true', 'false')},
						nillable:${field.isNillable()?string('true', 'false')},
						unique:false,
						<#if field.refEntity??>
							<#if field.refEntity.labelAttribute??>
								xrefLabelName: '${field.refEntity.labelAttribute.name?js_string}',
							</#if>
						xrefLabel: '${field.refEntity.name?js_string}',
						xrefEntityName: '${field.refEntity.name?lower_case?js_string}'
						</#if>
					}
					<#if field_has_next>,</#if>
				</#list>];
				
	//Get the label attribute
	<#if form.metaData.labelAttribute??>
		forms[${index}].meta.labelFieldName = '${form.metaData.labelAttribute.name?js_string}';
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
				'${field.name?js_string}': {
					remote: {
						url: '/api/v1/${form.metaData.name?lower_case?js_string}?q[0].operator=EQUALS&q[0].field=${field.name?js_string}',
						async: false,
						data: {
							'q[0].value': function() {//Bit cheesy, but it works, is appended to the url
								return $('#${field.name?js_string}').val();
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
				'${field.name?js_string}': {
					remote: "This ${field.name?js_string} already exists. It must be unique"
				},
			</#if>
		</#list>
	};
</script>
</#macro>

