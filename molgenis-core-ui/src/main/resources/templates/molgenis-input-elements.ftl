<#macro render field entity>

	<#assign fieldName=field.name?uncap_first/>
	
	<#-- ${field.type.enumType} -->
	
	<div class="control-group">
    	<label class="control-label" for="${fieldName}">${field.label} <#if field.nillable?string == 'false'>*</#if></label>
    	<div class="controls">
    		
			<#if field.type.enumType == 'BOOL'>
			<input type="checkbox" name="${fieldName}" id="${fieldName}" value="true" <#if entity.get(fieldName)!?string("true", "false") == "true">checked</#if>  <#if field.readOnly>disabled="disabled"</#if> >
	
			<#elseif field.type.enumType == 'TEXT' || field.type.enumType =='HTML'>
				<textarea name="${fieldName}" id="${fieldName}" <#if field.readOnly>disabled="disabled"</#if>>${entity.get(fieldName)!}</textarea>
	
			<#elseif field.type.enumType == 'XREF'>
				<input type="hidden" name="${fieldName}" id="${fieldName}">
				<script>
					$(document).ready(function() {
						$('#${fieldName}').select2({
							placeholder: 'Select ${field.xrefEntity.label!}',
							allowClear: true,
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
								<#if entity.get(fieldName)??>
									callback({id:'${entity.get(fieldName).idValue}', text: '${entity.get(fieldName).get(field.xrefLabelNames[0])!}'});
								<#else>
									callback({id:'', text: ''});
								</#if>
							}
						});
						
						<#if entity.get(fieldName)??>
							$('#${fieldName}').select2('val', '${entity.get(fieldName).idValue}');
						</#if>
						
						<#if field.readOnly>
							$('#${name}').select2('readonly', true);
						</#if>
					});
					
				</script>
			<#elseif field.type.enumType == 'MREF'>
				<input type="hidden" name="${fieldName}" id="${fieldName}">
				<script>
					$(document).ready(function() {
						var xrefs = [];
						<#if entity.get(fieldName)??>
							<#list entity.get(fieldName) as xrefEntity>
								xrefs.push({id:'${xrefEntity.idValue}', text:'${xrefEntity.get(field.xrefLabelNames[0])!}'});
							</#list>
						</#if>
								
						$('#${fieldName}').select2({
							placeholder: 'Select ${field.xrefEntity.label!}',
							allowClear: true,
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
							
						<#if field.readOnly>
							$('#${name}').select2('readonly', true);
						</#if>
					});
					
				</script>
				
			<#elseif field.type.enumType == 'DATE_TIME' || field.type.enumType == 'DATE'>
				<input type="text" name="${fieldName}" id="${fieldName}" placeholder="${field.label}" <#if field.readOnly>disabled="disabled"</#if> value="${entity.get(fieldName)!?string("yyyy-MM-dd'T'HH:mm:ssZ")}">
	
			<#else>
				<input type="text" name="${fieldName}" id="${fieldName}" placeholder="${field.label}" <#if field.readOnly>disabled="disabled"</#if> value="${entity.get(fieldName)!?string}">
	
			</#if>
		</div>
	</div>
	
</#macro>