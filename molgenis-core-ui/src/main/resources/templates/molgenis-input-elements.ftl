<#macro render field hasWritePermission entity='' forUpdate=true>

	<#assign fieldName = field.name/>
	<#assign readonly  = (forUpdate && field.readonly) || !hasWritePermission>
	<#assign nillable  = field.nillable>
	
	<div class="form-group">
		<div class="col-md-3">
    		<label class="control-label pull-right" for="${fieldName?html}">${field.label?html}&nbsp;<#if !nillable>*</#if></label>
    	</div>
    	
    	<div class="col-md-9">
    		<#if field.dataType.enumType == 'BOOL' && !field.nillable>
				<input type="checkbox" name="${fieldName?html}" id="${fieldName?html}" value="true" <#if entity!='' && entity.get(fieldName)?? && entity.get(fieldName)?string("true", "false") == "true">checked</#if>  <#if field.readonly || hasWritePermission?string("true", "false") == "false" >readonly="readonly"</#if>>
	
			<#elseif field.dataType.enumType == 'BOOL' && field.nillable>
					<label class="radio-inline"><input <#if entity!='' && entity.get(fieldName)?? && entity.get(fieldName)?string("true", "false") == "true">checked</#if> id="${fieldName?html}Yes" type="radio" name="${fieldName?html}" 	value="true"	<#if field.readonly || hasWritePermission?string("true", "false") == "false" >readonly="readonly"</#if>>Yes</label>
					<label class="radio-inline"><input <#if entity!='' && entity.get(fieldName)?? && entity.get(fieldName)?string("true", "false") == "false">checked</#if> id="${fieldName?html}No" type="radio" name="${fieldName?html}" 	value="false"	<#if field.readonly || hasWritePermission?string("true", "false") == "false" >readonly="readonly"</#if>>No</label>
					<label class="radio-inline"><input <#if entity!='' && !entity.get(fieldName)??>checked</#if> id="${fieldName?html}NA" type="radio" name="${fieldName?html}" value= <#if field.readonly || hasWritePermission?string("true", "false") == "false" >readonly="readonly"</#if>>N/A</label>
	
			<#elseif field.dataType.enumType == 'TEXT' || field.dataType.enumType =='HTML'>
				<textarea class="form-control" name="${fieldName?html}" id="${fieldName?html}" <#if readonly>readonly="readonly"</#if> <#if !nillable>required="required"</#if> ><#if entity!='' && entity.get(fieldName)??>${entity.get(fieldName)!?html}</#if></textarea>
			
			<#elseif field.dataType.enumType == 'XREF' || field.dataType.enumType == 'CATEGORICAL'>
				<input type="hidden" name="${fieldName?html}" id="${fieldName?html}" <#if !nillable>required="required"</#if> />
				<script>
					$(document).ready(function() {
						$('#${fieldName?js_string}').select2({
							width: '60%',
							placeholder: 'Select ${field.refEntity.name!?js_string}',
							allowClear: ${field.nillable?string('true', 'false')},
							query: function (query) {
								var queryResult = {more:false, results:[<#if nillable>{id:'', text:''}</#if>]};
								
								//Get posible xref values
								var restApi = new window.top.molgenis.RestClient(); 
								var url = '/api/v1/${field.refEntity.name!?lower_case?js_string}';
								var q = null;
								
								//When user first clicks in dropdown term is empty, then when user types we get called with the term, create query for it
								if (query.term.length > 0) {
									q = {q:[{field:'${field.refEntity.labelAttribute.name!?js_string}',operator:'LIKE',value:query.term}]};
								}
								
								restApi.getAsync(url, {q: q}, function(entities) {
									$.each(entities.items, function(index, entity) {
										queryResult.results.push({id:restApi.getPrimaryKeyFromHref(entity.href), text:entity['${field.refEntity.labelAttribute.name!?js_string}']});
									});
									query.callback(queryResult);
								});
							},
							<#if entity!='' && entity.get(fieldName)??>
							initSelection: function (element, callback) {
								callback({id:'<@formatValue field.refEntity.idAttribute.dataType.enumType entity.getEntity(fieldName).idValue />', text: '${entity.getEntity(fieldName).get(field.refEntity.labelAttribute.name)!?js_string}'});
							}
							</#if>
						});
						
						<#if entity!='' && entity.get(fieldName)??>
							$('#${fieldName?js_string}').select2('val', '<@formatValue field.refEntity.idAttribute.dataType.enumType entity.getEntity(fieldName).idValue />');
						</#if>
						
						<#if readonly>
							$('#${fieldName?js_string}').select2('readonly', true);
						</#if>
					});
					
				</script>
			<#elseif field.dataType.enumType == 'MREF'>
				<input type="hidden" name="${fieldName?html}" id="${fieldName?html}" <#if !nillable>required="required"</#if>>
				<script>
					$(document).ready(function() {
						var xrefs = [];
						<#if entity!='' && entity.get(fieldName)??>
							<#if entity.getEntities(fieldName)?has_content >
								<#list entity.getEntities(fieldName) as xrefEntity>
									xrefs.push({id:'<@formatValue field.refEntity.idAttribute.dataType.enumType xrefEntity.idValue />', text:'${xrefEntity.get(field.refEntity.labelAttribute.name)!?js_string}'});
								</#list>
							</#if>
						</#if>
								
						$('#${fieldName?js_string}').select2({
							width: '60%',
							placeholder: 'Select ${field.refEntity.name!?js_string}',
							allowClear: ${field.nillable?string('true', 'false')},
							multiple: true,
							query: function (query) {
								var queryResult = {more:false, results:[{id:'', text:''}]};
								
								//Get posible xref values
								var restApi = new window.top.molgenis.RestClient(); 
								var url = '/api/v1/${field.refEntity.name!?lower_case?js_string}';
								var q = null;
								
								//When user first clicks in dropdown term is empty, then when user types we get called with the term, create query for it
								if (query.term.length > 0) {
									q = {q:[{field:'${field.refEntity.labelAttribute.name!?js_string}',operator:'LIKE',value:query.term}]};
								}
								
								restApi.getAsync(url, {q: q}, function(entities) {
									$.each(entities.items, function(index, entity) {
										queryResult.results.push({id:restApi.getPrimaryKeyFromHref(entity.href), text:entity['${field.refEntity.labelAttribute.name!?js_string}']});
									});
									query.callback(queryResult);
								});
							},
							initSelection: function (element, callback) {
								callback(xrefs);
							}
						});
						
						$('#${fieldName?js_string}').select2('val', xrefs);
							
						<#if readonly>
							$('#${fieldName?js_string}').select2('readonly', true);
						</#if>
					});
					
				</script>
				
			<#elseif field.dataType.enumType == 'DATE_TIME'>
				<div class="input-group group-append date datetime">
  					<span class='input-group-addon'>
  						<span class="datepickerbutton glyp2icon-calendar glyphicon glyphicon-calendar"></span>
  					</span>
  					<input type="text" 
  						name="${fieldName?html}" 
  						id="${fieldName?html}" 
  						placeholder="${field.name?html}" 
  						data-date-format="YYYY-MM-DD'T'HH:mm:ssZZ"
						class="form-control<#if field.nillable> nillable</#if>" <#if readonly>disabled="disabled"</#if>
						<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string("yyyy-MM-dd'T'HH:mm:ssZ")?html}"</#if>
						<#if !nillable>required="required"</#if> 
						data-rule-date-ISO="true" />
							
  					<#if field.nillable>
						<span class='input-group-addon'>
							<span class="glyphicon glyphicon-remove empty-date-input clear-date-time-btn"></span>
						</span>
					</#if>
				</div>
			<#elseif field.dataType.enumType == 'DATE'>
				<div class="input-group group-append date dateonly">
					<span class='input-group-addon'>
						<span class='datepickerbutton glyp2icon-calendar glyphicon glyphicon-calendar'></span>
					</span>
					<input type="text" 
						name="${fieldName?html}" 
						id="${fieldName?html}" 
						placeholder="${field.name?html}"
						data-date-format="YYYY-MM-DD" 
						class="form-control<#if field.nillable> nillable</#if>" 
						<#if readonly>readonly="readonly"</#if> 
						<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string("yyyy-MM-dd")?html}"</#if> 
						<#if !nillable>required="required"</#if>
						data-rule-date-ISO="true" />
						
					<#if field.nillable>
						<span class='input-group-addon'>
							<span class='glyphicon glyphicon-remove empty-date-input clear-date-time-btn'></span>
						</span>
					</#if>
				</div>
				
			<#elseif field.dataType.enumType =='INT' || field.dataType.enumType = 'LONG'>
				<input type="number" class="form-control" data-rule-digits="true" name="${fieldName?html}" id="${fieldName?html}" placeholder="${field.name?html}" 
					<#if readonly>readonly="readonly"</#if> 
					<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)?c?html}"</#if> 
					<#if !nillable>required="required"</#if> />
			
			<#elseif field.dataType.enumType == 'SCRIPT'>
				<div style="width: 100%; height:250px" class="uneditable-input" id="${fieldName?html}-editor"></div>
				<#if entity!='' && entity.get(fieldName)??>
					<textarea class="form-control" name="${fieldName?html}" id="${fieldName?html}-textarea" <#if !nillable>required="required"</#if>>${entity.get(fieldName)!?html}</textarea>
				<#else>
					<textarea class="form-control" name="${fieldName?html}" id="${fieldName?html}-textarea" <#if !nillable>required="required"</#if>></textarea>
				</#if>
				<script>
					var editor = ace.edit("${fieldName?html}-editor");
					editor.setTheme("ace/theme/eclipse");
    				editor.getSession().setMode("ace/mode/r");
    					
    				var textarea = $("#${fieldName?html}-textarea").hide();
					editor.getSession().setValue(textarea.val());
					editor.getSession().on('change', function(){
  						textarea.val(editor.getSession().getValue());
					});	
				</script>
			<#elseif field.dataType.enumType == 'EMAIL'>
				<input type="email" class="form-control" data-rule-email="true" name="${fieldName?html}" id="${fieldName?html}" placeholder="${field.name?html}" 
					<#if readonly>readonly="readonly"</#if> 
					<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string?html}"</#if> 
					<#if !nillable>data-rule-required="true" </#if>/>
			<#else>
					<input type="text" class="form-control" name="${fieldName?html}" id="${fieldName?html}" placeholder="${field.name?html}" 
						<#if readonly>readonly="readonly"</#if> 
						<#if entity!='' && entity.get(fieldName)??>value="${entity.get(fieldName)!?string?html}"</#if> 
						<#if !nillable>required="required"</#if> 
						<#if field.dataType.enumType == 'DECIMAL'>data-rule-number="true"</#if>
						<#if field.dataType.enumType == 'HYPERLINK'>data-rule-url="true"</#if> />
			</#if>
		</div>
	</div>
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