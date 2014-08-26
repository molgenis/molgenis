<#macro render field hasWritePermission entity=''>

	<#assign fieldName=field.name/>
	
	<div class="form-group">
    	<label class="col-md-3 control-label" for="${fieldName}">${field.label} <#if field.nillable?string('true', 'false') == 'false'>*</#if></label>
    	<div class="col-md-9">
    		
    		<#if field.dataType.enumType == 'BOOL'>
				<input type="checkbox" name="${fieldName}" id="${fieldName}" value="true" <#if entity!='' && entity.get(fieldName)?? && entity.get(fieldName)?string("true", "false") == "true">checked</#if>  <#if field.readonly || hasWritePermission?string("true", "false") == "false" >disabled="disabled"</#if>  >
	
			<#elseif field.dataType.enumType == 'TEXT' || field.dataType.enumType =='HTML'>
				<textarea class="form-control" name="${fieldName}" id="${fieldName}" <#if field.readonly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <@validationOptions field /> ><#if entity!='' && entity.get(fieldName)??>${entity.get(fieldName)!?html}</#if></textarea>
			
			<#elseif field.dataType.enumType == 'XREF' || field.dataType.enumType == 'CATEGORICAL'>
				<input type="hidden" name="${fieldName}" id="${fieldName}" <@validationOptions field />>
				<script>
					$(document).ready(function() {
						$('#${fieldName}').select2({
							placeholder: 'Select ${field.refEntity.name!}',
							allowClear: ${field.nillable?string('true', 'false')},
							query: function (query) {
								var queryResult = {more:false, results:[<#if field.nillable?string('true', 'false') == 'true'>{id:'', text:''}</#if>]};
								
								//Get posible xref values
								var restApi = new window.top.molgenis.RestClient(); 
								var url = '/api/v1/${field.refEntity.name!?lower_case}';
								var q = null;
								
								//When user first clicks in dropdown term is empty, then when user types we get called with the term, create query for it
								if (query.term.length > 0) {
									q = {q:[{field:'${field.refEntity.labelAttribute.name!}',operator:'LIKE',value:query.term}]};
								}
								
								restApi.getAsync(url, {q: q}, function(entities) {
									$.each(entities.items, function(index, entity) {
										queryResult.results.push({id:restApi.getPrimaryKeyFromHref(entity.href), text:entity['${field.refEntity.labelAttribute.name!}']});
									});
									query.callback(queryResult);
								});
							},
							<#if entity!='' && entity.get(fieldName)??>
							initSelection: function (element, callback) {
								callback({id:'<@formatValue field.refEntity.idAttribute.dataType.enumType entity.getEntity(fieldName).idValue />', text: '${entity.getEntity(fieldName).get(field.refEntity.labelAttribute.name)!?html}'});
							}
							</#if>
						});
						
						<#if entity!='' && entity.get(fieldName)??>
							$('#${fieldName}').select2('val', '<@formatValue field.refEntity.idAttribute.dataType.enumType entity.getEntity(fieldName).idValue />');
						</#if>
						
						<#if field.readonly || hasWritePermission?string("true", "false") == "false">
							$('#${fieldName}').select2('readonly', true);
						</#if>
					});
					
				</script>
			<#elseif field.dataType.enumType == 'MREF'>
				<input type="hidden" name="${fieldName}" id="${fieldName}" <@validationOptions field />>
				<script>
					$(document).ready(function() {
						var xrefs = [];
						<#if entity!='' && entity.get(fieldName)??>
							<#list entity.getEntities(fieldName) as xrefEntity>
								xrefs.push({id:'<@formatValue field.refEntity.idAttribute.dataType.enumType xrefEntity.idValue />', text:'${xrefEntity.get(field.refEntity.labelAttribute.name)!?html}'});
							</#list>
						</#if>
								
						$('#${fieldName}').select2({
							placeholder: 'Select ${field.refEntity.name!}',
							allowClear: ${field.nillable?string('true', 'false')},
							multiple: true,
							query: function (query) {
								var queryResult = {more:false, results:[{id:'', text:''}]};
								
								//Get posible xref values
								var restApi = new window.top.molgenis.RestClient(); 
								var url = '/api/v1/${field.refEntity.name!?lower_case}';
								var q = null;
								
								//When user first clicks in dropdown term is empty, then when user types we get called with the term, create query for it
								if (query.term.length > 0) {
									q = {q:[{field:'${field.refEntity.labelAttribute.name!}',operator:'LIKE',value:query.term}]};
								}
								
								restApi.getAsync(url, {q: q}, function(entities) {
									$.each(entities.items, function(index, entity) {
										queryResult.results.push({id:restApi.getPrimaryKeyFromHref(entity.href), text:entity['${field.refEntity.labelAttribute.name!}']});
									});
									query.callback(queryResult);
								});
							},
							initSelection: function (element, callback) {
								callback(xrefs);
							}
						});
						
						$('#${fieldName}').select2('val', xrefs);
							
						<#if field.readonly || hasWritePermission?string("true", "false") == "false">
							$('#${fieldName}').select2('readonly', true);
						</#if>
					});
					
				</script>
				
			<#elseif field.dataType.enumType == 'DATE_TIME'>
				<div class="group-append datetime input-group">
					<input type="text" name="${fieldName}" id="${fieldName}" placeholder="${field.name}" 
						data-date-format='YYYY-MM-DDTHH:mm:ssZZ'
						class="form-control<#if field.nillable> nillable</#if>" 
						<#if field.readonly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> 
						<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string("yyyy-MM-dd'T'HH:mm:ssZ")}"</#if>
						<@validationOptions field /> />
					<#if field.nillable><span class='input-group-addon'>
						<span class='glyphicon glyphicon-remove empty-date-input clear-date-time-btn'></span></span>
					</#if>
					<span class='input-group-addon datepickerbutton'><span class='glyp2icon-calendar glyphicon glyphicon-calendar '></span></span>
				</div>
			<#elseif field.dataType.enumType == 'DATE'>
				<div class="group-append date input-group">
					<input type="text" name="${fieldName}" id="${fieldName}" placeholder="${field.name}"
						data-date-format='YYYY-MM-DD' 
						class="form-control<#if field.nillable> nillable</#if>" 
						<#if field.readonly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> 
						<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string("yyyy-MM-dd")}"</#if> 
						<@validationOptions field /> />
					<#if field.nillable><span class='input-group-addon'>
						<span class='glyphicon glyphicon-remove empty-date-input clear-date-time-btn'></span></span>
					</#if>
					<span class='input-group-addon datepickerbutton'><span class='glyp2icon-calendar glyphicon glyphicon-calendar '></span></span>
				</div>
				
			<#elseif field.dataType.enumType =='INT' || field.dataType.enumType = 'LONG'>
				<input type="number" class="form-control" name="${fieldName}" id="${fieldName}" placeholder="${field.name}" <#if field.readonly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)?c}"</#if> <@validationOptions field /> >
			
			<#elseif field.dataType.enumType == 'SCRIPT'>
				<#if entity!='' && entity.get(fieldName)??>
					<textarea class="form-control" name="${fieldName}" id="${fieldName}-textarea">${entity.get(fieldName)!?html}</textarea>
				<#else>
					<textarea class="form-control" name="${fieldName}" id="${fieldName}-textarea"></textarea>
				</#if>
				<div style="width: 100%; height:250px" class="uneditable-input" id="${fieldName}-editor"></div>
				<script>
					var editor = ace.edit("${fieldName}-editor");
					editor.setTheme("ace/theme/eclipse");
    				editor.getSession().setMode("ace/mode/r");
    					
    				var textarea = $("#${fieldName}-textarea").hide();
					editor.getSession().setValue(textarea.val());
					editor.getSession().on('change', function(){
  						textarea.val(editor.getSession().getValue());
					});	
				</script> 
			
			<#else>
				<input type="text" class="form-control" name="${fieldName}" id="${fieldName}" placeholder="${field.name}" <#if field.readonly || hasWritePermission?string("true", "false") == "false">disabled="disabled"</#if> <#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string?html}"</#if> <@validationOptions field /> >
			</#if>
		</div>
	</div>
	
</#macro>

<#macro validationOptions field>
	<#assign validations = []>
    
    <#if field.nillable?string('true', 'false') == 'false'>
    	<#assign validations = validations + ['required']>
    </#if>
    
    <#if field.dataType.enumType == 'INT' || field.dataType.enumType == 'LONG'>
    	<#assign validations = validations + ['digits']>
    </#if>
    
    <#if field.dataType.enumType == 'DECIMAL'>
    	<#assign validations = validations + ['number']>
    </#if>
    
    <#if field.dataType.enumType == 'EMAIL'>
    	<#assign validations = validations + ['email']>
    </#if>
    
    <#if field.dataType.enumType == 'HYPERLINK'>
    	<#assign validations = validations + ['url']>
    </#if>
    
    <#if validations?size &gt; 0>
    	class="<#list validations as validation>${validation} </#list>"
   	</#if>
</#macro>

<#macro formatValue fieldEnumType value>
<#compress>
	<#if fieldEnumType == 'INT' ||  fieldEnumType == 'LONG'>
		${value?c}
	<#else>
		${value}
	</#if>
</#compress>
</#macro>