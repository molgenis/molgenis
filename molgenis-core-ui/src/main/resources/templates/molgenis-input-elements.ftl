<#macro render field hasWritePermission entity=''>

	<#assign fieldName=field.name?uncap_first/>
	
	<div class="control-group">
    	<label class="control-label" for="${fieldName}">${field.label} <#if field.nillable?string('true', 'false') == 'false'>*</#if></label>
    	<div class="controls">
    		
    		<#if field.type.enumType == 'BOOL'>
				<input type="checkbox" name="${fieldName}" id="${fieldName}" value="true" <#if entity!='' && entity.get(fieldName)?? && entity.get(fieldName)?string("true", "false") == "true">checked</#if>  <#if field.readOnly || hasWritePermission?string("true", "false") == "false" >disabled="disabled"</#if>  >
	
			<#elseif field.type.enumType == 'TEXT' || field.type.enumType =='HTML'>
				<textarea name="${fieldName}" id="${fieldName}" <#if field.readOnly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <@validationOptions field /> ><#if entity!='' && entity.get(fieldName)??>${entity.get(fieldName)!?html}</#if></textarea>
	
			<#elseif field.type.enumType == 'XREF'>
				<input type="hidden" name="${fieldName}" id="${fieldName}" <@validationOptions field />>
				<script>
					$(document).ready(function() {
						$('#${fieldName}').select2({
							placeholder: 'Select ${field.xrefEntity.label!}',
							allowClear: ${field.nillable?string('true', 'false')},
							query: function (query) {
								var queryResult = {more:false, results:[<#if field.nillable?string('true', 'false') == 'true'>{id:'', text:''}</#if>]};
								
								//Get posible xref values
								var restApi = new window.top.molgenis.RestClient(); 
								var url = '/api/v1/${field.xrefEntity.name!?lower_case}';
								var q = null;
								
								//When user first clicks in dropdown term is empty, then when user types we get called with the term, create query for it
								if (query.term.length > 0) {
									q = {q:[{field:'${field.xrefLabelNames[0]!?uncap_first}',operator:'LIKE',value:query.term}]};
								}
								
								restApi.getAsync(url, null, q, function(entities) {
									$.each(entities.items, function(index, entity) {
										queryResult.results.push({id:restApi.getPrimaryKeyFromHref(entity.href), text:entity['${field.xrefLabelNames[0]!?uncap_first}']});
									});
									query.callback(queryResult);
								});
							},
							<#if entity!='' && entity.get(fieldName)??>
							initSelection: function (element, callback) {
									callback({id:'${entity.get(fieldName).idValue}', text: '${entity.get(fieldName).get(field.xrefLabelNames[0])!?html}'});
							}
							</#if>
						});
						
						<#if entity!='' && entity.get(fieldName)??>
							$('#${fieldName}').select2('val', '${entity.get(fieldName).idValue}');
						</#if>
						
						<#if field.readOnly || hasWritePermission?string("true", "false") == "false">
							$('#${fieldName}').select2('readonly', true);
						</#if>
					});
					
				</script>
			<#elseif field.type.enumType == 'MREF'>
				<input type="hidden" name="${fieldName}" id="${fieldName}" <@validationOptions field />>
				<script>
					$(document).ready(function() {
						var xrefs = [];
						<#if entity!='' && entity.get(fieldName)??>
							<#list entity.get(fieldName) as xrefEntity>
								xrefs.push({id:'${xrefEntity.idValue}', text:'${xrefEntity.get(field.xrefLabelNames[0])!?html}'});
							</#list>
						</#if>
								
						$('#${fieldName}').select2({
							placeholder: 'Select ${field.xrefEntity.label!}',
							allowClear: ${field.nillable?string('true', 'false')},
							multiple: true,
							query: function (query) {
								var queryResult = {more:false, results:[{id:'', text:''}]};
								
								//Get posible xref values
								var restApi = new window.top.molgenis.RestClient(); 
								var url = '/api/v1/${field.xrefEntity.name!?lower_case}';
								var q = null;
								
								//When user first clicks in dropdown term is empty, then when user types we get called with the term, create query for it
								if (query.term.length > 0) {
									q = {q:[{field:'${field.xrefLabelNames[0]!?uncap_first}',operator:'LIKE',value:query.term}]};
								}
								
								restApi.getAsync(url, null, q, function(entities) {
									$.each(entities.items, function(index, entity) {
										queryResult.results.push({id:restApi.getPrimaryKeyFromHref(entity.href), text:entity['${field.xrefLabelNames[0]!?uncap_first}']});
									});
									query.callback(queryResult);
								});
							},
							initSelection: function (element, callback) {
								callback(xrefs);
							}
						});
						
						$('#${fieldName}').select2('val', xrefs);
							
						<#if field.readOnly || hasWritePermission?string("true", "false") == "false">
							$('#${name}').select2('readonly', true);
						</#if>
					});
					
				</script>
				
			<#elseif field.type.enumType == 'DATE_TIME' || field.type.enumType == 'DATE'>
				<div class="input-append date">
					<input readonly type="text" name="${fieldName}" id="${fieldName}" placeholder="${field.label}" <#if field.nillable>class="nillable"</#if> <#if field.readOnly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string("yyyy-MM-dd'''T'''HH:mm:ssZ")}"</#if> <@validationOptions field /> >
					<#if field.nillable><span class="add-on-workaround"><i class="icon-remove empty-date-input"></i></span></#if> <span class="add-on"><i></i></span>
				</div>
				
			<#elseif field.type.enumType =='INT' || field.type.enumType = 'LONG'>
				<input type="number" name="${fieldName}" id="${fieldName}" placeholder="${field.label}" <#if field.readOnly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)}"</#if> <@validationOptions field /> >
	
			<#else>
				<input type="text" name="${fieldName}" id="${fieldName}" placeholder="${field.label}" <#if field.readOnly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string?html}"</#if> <@validationOptions field /> >
	
			</#if>
		</div>
	</div>
	
</#macro>

<#macro validationOptions field>
	<#assign validations = []>
    
    <#if field.nillable?string('true', 'false') == 'false'>
    	<#assign validations = validations + ['required']>
    </#if>
    
    <#if field.type.enumType == 'INT' || field.type.enumType == 'LONG'>
    	<#assign validations = validations + ['digits']>
    </#if>
    
    <#if field.type.enumType == 'DECIMAL'>
    	<#assign validations = validations + ['number']>
    </#if>
    
    <#if field.type.enumType == 'EMAIL'>
    	<#assign validations = validations + ['email']>
    </#if>
    
    <#if field.type.enumType == 'HYPERLINK'>
    	<#assign validations = validations + ['url']>
    </#if>
    
    <#if validations?size &gt; 0>
    	class="<#list validations as validation>${validation} </#list>"
   	</#if>
</#macro>