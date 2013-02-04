<#include "Layout.ftl"/>
<#macro form_header screen> 
<div class="form_header" id="${screen.getName()}">
	<table width="100%">
		<tr>
			<td colspan="3" class="form_title">
				<#if screen.header?exists>${screen.header}<#else>${screen.label}</#if>
			</td>
		</tr>
		<tr>
		<!--
			<td colspan="3" class="form_title">
	<#list screen.getFilters() as filter>
				<label><#if filter_index=0>where: <#else></#if>${filter}</label><img height="16" class="navigation_button" src="img/cancel.png" alt="Cancel" onclick="setInput('${screen.name}_form','_self','','${screen.name}','filter_remove','iframe'); document.forms.${screen.name}_form.filter_id.value='${filter_index}'; document.forms.${screen.name}_form.submit();" title="remove filter"/>
	</#list>
			</td>
        -->
		</tr>
		<tr>
			<td width="33%" align="left" style="white-space: nowrap">				
				<!--div class="formmenu" formmenu doesn't exist in menu.css, form_menu does but has style display:none, so it is hidden. WTF? -->
					<table class="formmenutable">
						<tr>			
	<#list screen.getMenus() as menu>
							<td class="menuitem" id="${screen.name}_menu_${menu.name}" onclick="mopen('${screen.name}_menu_${menu.name}Sub');">
								${menu.label}
								<img src="img/pulldown.gif" alt="pulldown"><br />
								<div class="submenu" id="${screen.name}_menu_${menu.name}Sub">
									<table>
		<#list menu.getCommands() as command>
			<#if command.isVisible()>
										<tr><td id="${screen.name}_${command.name}_submenuitem" class="submenuitem" onclick="${command.getJavaScriptAction()}"><#if command.icon?exists><img src="${command.icon}" align="left" alt="${command.label}"><#else>&nbsp; </#if>${command.label}</td></tr>
			</#if>
		</#list>
									</table>
								</div>											
							</td>
	</#list>
						</tr>
					</table>					
				<!--/div-->			
	 		</td>				
			<td width="34%" class="form_title">
				<img class="navigation_button" id="first_${screen.name}" src="img/first.png" onclick="setInput('${screen.name}_form','_self','','${screen.name}','first','iframe'); document.forms.${screen.name}_form.submit();" title="go to first record"/>
				<img class="navigation_button" id="prev_${screen.name}" src="img/prev.png" onclick="setInput('${screen.name}_form','_self','','${screen.name}','prev','iframe'); document.forms.${screen.name}_form.submit();" title="go to previous record"/>
	<#assign from = screen.offset + 1>
	<#if from &gt; screen.count><#assign from = screen.count></#if>
	<#if screen.mode.toString() == "listview">
		<#assign to = screen.offset + screen.limit>
		<#if to &gt; screen.count><#assign to = screen.count></#if>
				<label>${from}<#if screen.mode.toString() == "listview"> - ${to}</#if> of ${screen.count}</label>
	<#else>
				<label>${from} of ${screen.count}</label>
	</#if>
				<img class="navigation_button" id="next_${screen.name}" src="img/next.png" onclick="setInput('${screen.name}_form','_self','','${screen.name}','next','iframe'); document.forms.${screen.name}_form.submit()" title="go to next record"/>
				<img class="navigation_button" id="last_${screen.name}" src="img/last.png" onclick="setInput('${screen.name}_form','_self','','${screen.name}','last','iframe'); document.forms.${screen.name}_form.submit()" title="go to last record"/>	
			</td>	
			<td width="33%">
				<!--to counter the menu size on the right-->
			</td>											
		</tr>		 				
	</table>
	<#--messages-->
	<#list screen.getMessages() as message>
		<#if message.success>
	<p class="successmessage">${message.text}</p>
		<#else>
	<p class="errormessage">${message.text}</p>
		</#if>
	</#list>
</div>

	<!--enforce validation-->
</#macro>

<#macro form_toolbar screen>
<div align="right">
		<!--toolbar-->
		<#list screen.getToolbar() as command>
			<img id="${screen.name}_${command.name}" class="navigation_button" src="${command.icon}" onclick="${command.getJavaScriptAction()}" alt="${command.label}" title="${command.label}"/>&nbsp;
		</#list>	
		<!--search box-->
		<label>Search:</label><select id="${screen.name}_filter_attribute" title="choose attribute" name="__filter_attribute" style="display:none">
			<option value="all">Any field</option>
		<#list screen.getNewRecordForm().inputs as input><#if !input.isHidden()>
			<option value="${screen.getSearchField(input.name)}">${input.label}</option>
		</#if></#list>
			<option value="searchIndex">Search Index</option>
		</select>
		<select id="${screen.name}_filter_operator" title="choose search operator" name="__filter_operator" style="display:none">
			<option value="LIKE">contains</option>	
			<option value="EQUALS">equals</option>
			<option value="NOT">not equal</option>
			<option value="LESS">less than</option>
			<option value="LESS_EQUAL">less equal</option>
			<option value="GREATER_EQUAL">greater equal</option>
			<option value="GREATER">greater than</option>
		</select>
		<input title="fill in search term" type="text" name="__filter_value" onfocus="${screen.name}_filter_attribute.style.display='inline'; ${screen.name}_filter_operator.style.display='inline';" onkeypress="if (event.keyCode == 13){setInput('${screen.name}_form','_self','','${screen.name}','filter_add','iframe'); document.forms.${screen.name}_form.submit(); return false;}">					
		<img class="navigation_button" src="img/filter.png" alt="Add filter" onclick="setInput('${screen.name}_form','_self','','${screen.name}','filter_add','iframe'); document.forms.${screen.name}_form.submit();"/>		
</div>
</#macro>

<#macro form_new screen>
<body>
	<p class="form_header">Add new ${screen.label}</p>
	<table>
		<form action="" method="post" enctype="multipart/form-data" name="${screen.name}_new">
			<input type="hidden" name="__target" value="${screen.name}"/>
			<input type="hidden" name="__action" value="add"/> 
	<#assign requiredcount = 0 />
	<#assign required = "" />
	<#list screen.getNewRecordForm() as input>
		<#if !input.isHidden()>
			<tr>
				<td title="${input.description}"><label for="${input.name}">${input.label}<#if !input.isNillable()  && !input.isReadonly()> *</#if></label></td>
				<td>${input.toHtml()}</td>
			</tr>
		<#else>
			${input.toHtml()}
		</#if>		
		<#if input.uiToolkit=='ORIGINAL' && !input.isNillable() && !input.isHidden() && !input.isReadonly()>
			<#if requiredcount &gt; 0><#assign required = required + "," /></#if>
			<#assign required = required + "document.forms." + screen.name +"_new."+ input.id />
			<#assign requiredcount = requiredcount + 1 />
		</#if>
	</#list>
			<!--<tr><td><br /><label>Number of copies: </label></td>
		
			<td><br /><input name="__batchadd" value="1" size="4"/></td></tr>-->
		</form>
	</table>
	<p align="right">
		<img class="edit_button" src="img/save.png" alt="Save" onclick="if( validateForm(${screen.name}_new,new Array(${required})) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+Math.random();} document.forms.${screen.name}_new.target = window.opener.name; document.forms.${screen.name}_new.submit(); window.close();}" title="save new record"/>
		<img class="edit_button" src="img/cancel.png" alt="Cancel" onclick="window.close();" title="cancel adding a new record"/>
	</p>
</body>
</#macro>
<#--
<#macro form_popup screen>
<#if screen.currentCommand?exists && screen.getCurrentCommand().getInputs()?exists >
<body>
	<p class="form_header">${screen.currentCommand.label}</p>
	<table>
		<form action="" method="post" enctype="multipart/form-data" name="molgenis_popup">
			<input type="hidden" name="__target" value="${screen.name}"/>
			<input type="hidden" name="__action" value="${screen.currentCommand.name}"/> 
	<#assign requiredcount = 0 />
	<#assign required = "" />
	<#list screen.getCurrentCommand().getInputs() as input>
		<#if !input.isHidden()>
			<tr>
				<td title="${input.description}"><label>${input.label}</label></td>
				<td>${input.toHtml()}<#if !input.isNillable()  && !input.isReadonly()> *</#if></td>
			</tr>
		<#else>
			${input.toHtml()}
		</#if>		
		<#if !input.isNillable() && !input.isHidden() && !input.isReadonly()>
			<#if requiredcount &gt; 0><#assign required = required + "," /></#if>
			<#assign required = required + "document.forms.molgenis_popup."+ input.id />
			<#assign requiredcount = requiredcount + 1 />
		</#if>
	</#list>
	
<script language="JavaScript" type="text/javascript">
var molgenis_required = new Array(${required});
</script>
		</form>
	</table>
	<p align="right">
<#list screen.getCurrentCommand().getActions() as input>
	${input.toHtml()}
</#list>
	</p>
</body>
<#else>
	ERROR no command dialog to bee seen
</#if>
</#macro>
-->

<#macro form_filter screen>
<body>
	<p class="form_header">Add new Filter</p>
	<table>					
		<form action="" method="post" enctype="multipart/form-data" name="${screen.name}_filter">
			<input type="hidden" name="__target" value="${screen.name}"/>
			<input type="hidden" name="__action" value="filter_add" />	
			<tr>
				<td><label>Field:</label></td>
				<td>
					<select name="attribute">
	<#list screen.getNewRecordForm() as input>
					<option value="${input.name}">${input.label}</option>
	</#list>
					</select>
				</td>
			</tr>
			<tr>
				<td><label>Operator:</label></td>
				<td>
					<select name="operator">
					<option value="EQUALS">=</option>
					<option value="LIKE">LIKE</option>	
					<option value="NOT">!=</option>
					<option value="LESS">&lt;</option>
					<option value="LESS_EQUAL">&lt;=</option>
					<option value="GREATER_EQUAL">&gt;=</option>
					<option value="GREATER">&gt;</option>
					</select>
				</td>
			</tr>
			<tr><td><label>Value:</label></td><td><input type="text" name="value"></td></tr>
			<tr>
				<td></td>
				<td >					
					<img class="edit_button" src="img/save.png" alt="Add filter" onclick=" document.forms.${screen.name}_filter.submit(); window.opener.location.href = window.opener.location.href; window.close();"/>
					<img class="edit_button" src="img/cancel.png" alt="Cancel" onclick="window.close();" title="cancel adding a new filter"/>				
				</td>
			</tr>
		</table>
	</form>
</body>
</#macro>

<#macro form_massupdate command>
<#assign massupdate = command.screen.getSelectedIds()>
<#assign screen = command.screen>
<body onload="<#list screen.getNewRecordForm().inputs as input><#if !input.isHidden()>${screen.name}_massupdate.${input.name}.disabled = true; </#if></#list>">
	<p class="form_header">Update multiple ${screen.label} records</p>
	<#if massupdate?has_content>
	You are about to update the records with the following id's:
		<#list massupdate as id>
	'${id}'
		</#list>
	<table>
		<form action="" method="post" enctype="multipart/form-data" name="${screen.name}_massupdate">
			<input type="hidden" name="__target" value="${screen.name}"/>
			<input type="hidden" name="__action" value="${command.name}"/> 
	<#list massupdate as id>
			<input type="hidden" name="massUpdate" value="${id}">
	</#list>
	<#list screen.getInputs() as input>
		<#if !input.isHidden()>
			<tr>
				<td><input type="checkbox" name="use_${input.name}" <#if input.readonly>DISABLED</#if> onclick="this.form.${input.name}.disabled = !this.form.${input.name}.disabled;" /></td>
				<td title="${input.description}"><label>${input.label}</label></td>
				<td>${input.toHtml()}</td>
			</tr>
		</#if>
	</#list>
		</form>
	</table>
	<p align="right">
		<!--<img class="edit_button" src="img/save.png" alt="Save" onclick="document.forms.${screen.name}_massupdate.submit(); window.opener.location.href = window.opener.location.href; window.close();" title="update records"/>-->
		<img class="edit_button" src="img/save.png" alt="Save" onclick="document.forms.${screen.name}_massupdate.target = window.opener.name; document.forms.${screen.name}_massupdate.submit(); window.close();" title="update records"/>
		<img class="edit_button" src="img/cancel.png" alt="Cancel" onclick="window.close();" title="cancel updating records"/>
	</p>
	
	<#else>
	You did not check any records.
	<p align="right">
		<input type="image" src="img/cancel.png" alt="Cancel" name="action_cancel" value="Cancel" onclick="window.close();" title="cancel adding a new record"/>
	</p>
	</#if>
</body>
</#macro>

<#macro editview screen>
	<#list screen.getRecordInputs() as record>
<table><tr><td>
<table>
		<#assign requiredcount = 0 />
		<#assign required = "" />
		<#assign readonly = "true" />
		<#list record.inputs as input>
			<#if !input.isReadonly()>
				<#assign readonly = "false" />
			</#if>
			
			<#if !input.isHidden()>
	<tr <#if input.collapse>class="${screen.name}_collapse" id="${screen.name}_collapse_tr_id" </#if>>
		<td style="vertical-align:middle"><label class="ui-widget">${input.label} <#if !input.isNillable() && !input.isReadonly()> *</#if></label></td>
				<#if screen.readonly >
					<#if input.getTarget() != "" && input.getObject()?exists >
					<td class="link" onClick="setInput('${screen.name}_form','_self','','${input.getTarget()}','xref_select','iframe'); document.forms.${screen.name}_form.attribute.value='${input.getTargetfield()}'; document.forms.${screen.name}_form.operator.value='EQUALS'; document.forms.${screen.name}_form.value.value='${input.getObject()}'; document.forms.${screen.name}_form.submit();">${input.getHtmlValue()}</td>	
					<#else>
		<td><div  class="recordview_datavalue">${input.getValue()?replace('\n','<br />')}&nbsp;</div></td>
					</#if>
				<#else>
		<td>${input.toHtml()}</td>
				</#if>
	</tr>
			<#else>
			${input.toHtml()}
			</#if>
			
			<#if input.uiToolkit=='ORIGINAL' && !input.isNillable() && !input.isHidden() && !input.isReadonly()>
				<#if requiredcount &gt; 0><#assign required = required + "," /></#if>
				<#assign required = required + "document.forms." + screen.name + "_form." + input.id />
				<#assign requiredcount = requiredcount + 1 />
			</#if>
		</#list>
<#--show collapse button if collapsed items-->
<#list record.inputs as input>
	<#if input.collapse><tr><td colspan="2"><script>toggleCssClass("${screen.name}_collapse");</script>
	<input type="button" id="${screen.name}_collapse_button_id" onClick="toggleCssClass('${screen.name}_collapse'); if(this.value == 'Hide details') this.value='Show details'; else this.value='Hide additional fields'" value="Show additional fields"/></td></tr><#break/></#if>
</#list>		
</table></td><td  class="edit_button_area">
<#if readonly != "true">
<img class="edit_button" id="save_${screen.name}" src="img/save.png" alt="Save" onclick="if ($('#${screen.name}_form').valid() && validateForm(document.forms.${screen.name}_form,new Array(${required}))) {setInput('${screen.name}_form','_self','','${screen.name}','update','iframe'); document.forms.${screen.name}_form.submit();}" title="save the changes" />
<#-->image class="edit_button" src="img/save.png" alt="Save" onclick="setInput('${screen.name}_form','_self','','${screen.name}','update','iframe'); document.forms.${screen.name}_form.submit();}" title="save the changes" /-->
<img class="edit_button" id="reset_${screen.name}" src="img/reset.png" alt="Reset" onClick="setInput('${screen.name}_form','_self','','${screen.name}','listview','iframe'); document.forms.${screen.name}_form.submit();" title="stop editing and go to list view" />
<img class="edit_button" id="delete_${screen.name}" src="img/delete.png" alt="Delete" onclick="if (confirm('You are about to delete a record. If you click [yes] you won\'t be able to undo this operation.')) { setInput('${screen.name}_form','_self','','${screen.name}','remove','iframe'); document.forms.${screen.name}_form.submit(); }" title="delete current record" />
</#if>
</td></tr></table>		
	</#list>
<br />
</#macro>

<#macro listview screen>
<#if screen.description??>${screen.description}<br><br></#if>
<table class="listtable">
	<#assign offset = screen.offset />
	<#assign count = 0 />
	<#list screen.getRecordInputs() as record>
		<#if count == 0>		
	<tr>
		<#--empty headers for the browse button and tick boxes -->
		<th><label>&nbsp;</label></th>
			<th align="left"><label><#-- 'select all' tick box -->
				<#list screen.getNewRecordForm().inputs as input>
					<#if input.getName() == screen.getIdField()>
						<input title="select all visible" type="checkbox" name="checkall" id="checkall" onclick="Javascript:checkAll('${screen.name}_form','massUpdate')" />
					</#if>
				</#list>
			</label></th>		
			<#list record.inputs as input>				
				<#if screen.getController().getClass().getSimpleName() != "FormController" && screen.getController().getClass().getSuperclass().getSimpleName() != "FormController">
					<#if input.isHidden()>
					<#else>
					<th>
						<label class="tableheader" title="${input.getDescription()}">${input.getLabel()}</label></th>
					</#if>
				<#else>
					<#if input.isHidden()>
			<#--<img src="img/open.png" title="show ${input.getLabel()}" onclick="setInput('${screen.name}_form','_self','','${screen.name}','showColumn','iframe'); document.forms.${screen.name}_form.attribute.value ='${input.getName()}'; document.forms.${screen.name}_form.submit();" />-->
					<#else>
			<#--<img src="img/close.png" title="hide ${input.getLabel()}" onclick="setInput('${screen.name}_form','_self','','${screen.name}','hideColumn','iframe'); document.forms.${screen.name}_form.attribute.value='${input.getName()}'; document.forms.${screen.name}_form.submit();" />-->
			<th><label class="tableheader" onclick="setInput('${screen.name}_form','_self','','${screen.name}','sort','iframe'); document.forms.${screen.name}_form.__sortattribute.value='${input.getName()}'; document.forms.${screen.name}_form.submit();">
					${input.getLabel()}<#if screen.getSort() == input.getName()> <#if screen.getSortMode().toString() == "SORTASC"><img src="img/sort_asc.gif"><#else><img src="img/sort_desc.gif"></#if></#if>
			</label>
					</th>
					</#if>
				</#if>

			</#list>
	</tr>	
		</#if>
		<#assign count = count + 1>
		<#assign offset = offset + 1>
		<#assign rowcolor = offset % 2>		
		<#assign readonly = "*" />
		<#list record.inputs as input>
			<#if !input.isReadonly()>
				<#assign readonly = "" />
			</#if>
		</#list>
<tr class="form_listrow${rowcolor}">
	<td>
		<label>${offset}. 
		<#if readonly == "*" >
			<img class="edit_button" src="img/recordview.png" title="view record" alt="edit${offset}" onClick="setInput('${screen.name}_form','_self','','${screen.name}','editview','iframe'); document.forms.${screen.name}_form.__offset.value='${offset?string.computer}'; document.forms.${screen.name}_form.submit();">${readonly}</label>
		<#else>
			<img class="edit_button" src="img/editview.gif" title="edit record" alt="edit${offset}" onClick="setInput('${screen.name}_form','_self','','${screen.name}','editview','iframe'); document.forms.${screen.name}_form.__offset.value='${offset?string.computer}'; document.forms.${screen.name}_form.submit();">${readonly}</label>
		</#if>
	</td>
		
		<td><input type="checkbox" name="massUpdate<#if record.entity.readonly>_readonly</#if>" value="${record.entity.idValue}"></td>
		<#list record.inputs as input>
				<#if input.isHidden()>
				<#else>
					<#if input.getTarget() != "" && input.getObject()?exists >
	<td class="link" onClick="setInput('${screen.name}_form','_self','','${input.getTarget()}','xref_select','iframe'); document.forms.${screen.name}_form.attribute.value='${input.getTargetfield()}'; document.forms.${screen.name}_form.operator.value='EQUALS'; document.forms.${screen.name}_form.value.value='${input.getObject()}'; document.forms.${screen.name}_form.submit();">${input.getHtmlValue()}</td>
					<#elseif input.getClass().getSimpleName() == "FileInput">
	<td  title="${input.getDescription()}">${input.getValue()}</td>	
					<#elseif input.getClass().getSimpleName() == "HyperlinkInput" || input.getClass().getSimpleName() == "EmbeddedInput"  || input.getHtmlValue()?length &lt; 100>
	<td title="${input.getDescription()}">${input.getHtmlValue()}</td>
					<#else>
	<td title="${input.getDescription()}">${input.getHtmlValue(100)}<b class="link" onClick="alert('${input.getJavaScriptValue()}');">...</b></td>
					</#if>
				</#if>
		</#list>
</tr>
	</#list>
	</table>
	<label> * = this record is readonly.</label>
</#macro>

<#macro form_upload screen>
<body>
	<p class="form_header">Upload CSV for ${screen.label}</p>
	<form action="" method="post" enctype="multipart/form-data" name="${screen.name}_upload">
		<input type="hidden" name="__target" value="${screen.name}"/>
		<input type="hidden" name="__action" value="upload"/> 
		<table>
		<tr><td colspan="2"><i>Set constants (overrides uploaded data).</i></td></tr>
		<#list screen.getNewRecordForm() as input>
		<tr><td><label>${input.label}<#if !input.isNillable()  && !input.isReadonly()> *</#if></label></td><td>${input.toHtml()}</td></tr>
		</#list>
		<!--<tr><td><br /><label>Number of copies: </label></td><td><br /><input name="__batchadd" value="1" size="4"/></td></tr>-->		
		<tr><td colspan="2"><i>Add CSV data.</i></td></tr>					
		<tr><td><label>CSV data</label></td><td><textarea name="__csvdata" cols="80" rows="20"></textarea></td></tr>
		<tr><td colspan="2" class="edit_button_area">
		<!--<img class="edit_button" src="img/save.png" alt="Save" onclick="document.forms.${screen.name}_upload.submit(); window.opener.location.href = window.opener.location.href; window.close();" title="upload csv"/>-->
		<img class="edit_button" src="img/save.png" alt="Save" onclick="if( window.opener.name == '' ){ window.opener.name = 'molgenis'+Math.random();} document.forms.${screen.name}_upload.target = window.opener.name; document.forms.${screen.name}_upload.submit(); window.close();" title="upload csv"/>
		<img class="edit_button" src="img/cancel.png" alt="Cancel" onclick="window.close();" title="cancel upload csv"/>
		</td></tr>
		</table>
	</form>
</body>
</#macro>

<#if show == "popup">
	<#--@form_popup screen/-->
	${screen.getCurrentCommand().render()}	
<#elseif show == "newrecord">
	<@molgenis_header screen/>
	<@form_new screen/>
	<@molgenis_footer />
<#elseif show == "upload">
	<@molgenis_header screen/>
	<@form_upload screen/>
	<@molgenis_footer />
<#elseif show == "filterrecord">
	<@molgenis_header screen/>
	<@form_filter screen/>
	<@molgenis_footer />
<#elseif show == "massupdate">
	<@molgenis_header screen/>
	MASSUPDATE
	<@form_massupdate screen=screen.getCurrentCommand() massupdate=massupdate />
	<@molgenis_footer />
<#else>
	<!--FormScreen ${screen.getName()}-->
	<div class="formscreen" id="${screen.getName()}_screen">
		<a name="${screen.name}"/>
		<div style="position: absolute; left: -10000px; top: -10000px;" id="popup_${screen.getName()}" class="popup" position="absolute"></div>
		<form name="${screen.name}_form"  id="${screen.name}_form" target="#${screen.name}" action="" method="post" style="margin: 0px;" enctype="multipart/form-data">
			<input type="hidden" name="__target"  value="${screen.name}" />
			<input type="hidden" name="__action">
			<input type="hidden" name="__filename" />
			<input type="hidden" name="__show">
			<input type="hidden" name="__offset">
			<input type="hidden" name="__sortattribute">
			<!--input type="hidden" name="operator" />
			<input type="hidden" name="value" />-->
			<input type="hidden" name="limit">
			<input type="hidden" name="filter_id" />
			<@form_header screen/>
			<!-- put in table so we can collapse the content panel-->
			<@form_toolbar screen/>
			<!--<td class="form_collapse" style="width: 12px;float:right;clear:right; margin:5px;"><img src="img/minus.png" id="${screen.name}_toggleImage" onclick="javascript:toggleForm('${screen.name}')"/></td>-->
			
			<#if screen.getFilters()?size gt 0>
				<div style="float: right;font-size-adjust: 0.5;font-style:italic;background:#C0C0C0;border: .2em dotted #000;padding:4px;">
					Search results where:
					<#list screen.getFilters() as filter>			
						<b>${filter}</b> <img id="remove_filter_${filter_index}" height="16" class="navigation_button" src="img/cancel.png" alt="Cancel" onclick="setInput('${screen.name}_form','_self','','${screen.name}','filter_remove','iframe'); document.forms.${screen.name}_form.filter_id.value='${filter_index}'; document.forms.${screen.name}_form.submit();" title="remove filter"/>
					<#if filter_has_next> and </#if>
					</#list>
				</div>
			</#if>

			<div class="screenbody" style="clear:both">
				<div class="screenpadding">
					<#if screen.mode.toString() == "editview">
						<@editview screen />
					<#else> 
						<div style="overflow-x: auto;">
							<@listview screen /> 
						</div>
					</#if>
				</div>
			</div>
		</form>		
		<script>
			$('#${screen.name}_form').validate();
		</script>
							
		<!-- subforms -->
			<#if (screen.mode.toString() == "editview") && screen.count &gt; 0>
				<#list screen.children as subscreen>
					<div class="subscreen">
						<@layout subscreen/>
					</div>
				</#list>
			</#if>
		<!-- end of FormScreen ${screen.getName()}-->
	</div>	
</#if>