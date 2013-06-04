<#macro PersonToUser screen>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action">
	<!--need to be set to "true" in order to force a download-->
	<input type="hidden" name="__show">
	
<!-- this shows a title and border -->
	<div class="formscreen">
		<div class="form_header" id="${screen.getName()}">
		${screen.label}
		</div>
		
		<#--optional: mechanism to show messages-->
		<#list screen.getMessages() as message>
			<#if message.success>
		<p class="successmessage">${message.text}</p>
			<#else>
		<p class="errormessage">${message.text}</p>
			</#if>
		</#list>

<#if screen.myModel?exists>
	<#assign modelExists = true>
	<#assign model = screen.myModel>
<#else>
	No model. An error has occurred.
	<#assign modelExists = false>
</#if>




<#if model.personList?size == 0>
<table cellpadding="20">
	<tr>
		<td>
			<b>No persons available for transformation into MolgenisUsers. Add them in the table 'Person'.</b>
		</td>
	</tr>
</table>		
<#else>

<table cellpadding="10">

	<tr>
		<td>
			This plugin transforms Persons to MolgenisUsers. The person is deleted and subsequently re-added as an application user with login permissions.
			If you have groups defined, you can put the new user in a group directly. Please note:
				<ul>
					<li>The password of the user is set to <b>changeme</b></li>
					<li>The user is immediately active</li>
				</ul>
			
		</td>
	</tr>
	<tr>
		<td>
			Select a person to be upgraded:
			<select name="personId">
			<#list model.personList as p>
				<option value=${p.id?c}>${p.name}</option>
			</#list>
			</select>
		</td>
	</tr>


	<#if model.groupList?size != 0>
	<tr>
		<td>
			Put the user in group:
			<select name="groupId">
			<#list model.groupList as g>
				<#if g.name != 'system' && g.name != 'AllUsers'>
				<option value="${g.id?c}">${g.name}</option>
				</#if>
			</#list>
			<option value="-1">Do not put in group</option>
			</select>
		</td>
	</tr>
	</#if>

	<tr>
		<td>
			Click to proceed: <input type="submit" value="Upgrade" onclick="document.forms.${screen.name}.__action.value = 'upgrade'; document.forms.${screen.name}.submit();">
		</td>
	</tr>

</table>

</#if>


<#--end of your plugin-->	
			</div>
		</div>
	</div>
</form>
</#macro>
