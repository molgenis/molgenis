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
						xref:${field.isXRef()?string},
						mref:${field.isMRef()?string},
						type:'${field.type.enumType}',
						readOnly:${field.isReadOnly()?string},
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
</script>