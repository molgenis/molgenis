<script>
	//Base url of the plugin
	var CONTEXT_URL = '${context_url}';
	
	var form = {
		title: '${form.title}',
		hasWritePermission: ${form.getHasWritePermission()?string},
		<#if form.primaryKey??>
			primaryKey: '${form.primaryKey}'
		</#if>
	}
	
	//Build metadata to be used by the js
	form.meta = {
		name:'${form.metaData.name}'
	}
	
	//The fieldnames of the entity
	form.meta.fields = [<#list form.metaData.fields as field>
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
							
	form.meta.getField = function(name) {
		for (var i = 0; i < form.meta.fields.length; i++) {
			if (form.meta.fields[i].name == name) {
				return form.meta.fields[i];
			}
		}
		
		return null;
	}
	
	form.meta.getXRefAndMRefFieldNames = function() {
		var fields = [];
		for (var i = 0; i < form.meta.fields.length; i++) {
			if (form.meta.fields[i].xref || form.meta.fields[i].mref) {
				fields.push(form.meta.fields[i].name);
			}
		}
		
		return fields;
	}
	
	//Remote validation rules for unique fields (check if unique)
	var remoteRules = {
		<#list form.metaData.fields as field>
			<#if field.isUnique()?string('true', 'false') == 'true'>
				${field.name?uncap_first}: {
					remote: {
						url: '/api/v1/protocol?q[0].operator=EQUALS&q[0].field=${field.name?uncap_first}',
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