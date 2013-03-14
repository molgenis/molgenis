<#macro TokenManager screen>
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


<table cellpadding="10">
	<tr>
		<td>
			<table>
				<tr>
					<td>
						<h2>Create new tokens:</h2>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>
			<table>
				<tr>
					<td>
					Create 
					
					 <select name="amountOfTokens">
						<#list 1..10 as x>
							<option value="${x}">${x}</option>
						</#list>
					</select>
			
					
					security tokens, valid for 
					
					<select name="amountOfDaysValid">
						<#list 0..6 as x>
							<option value="${x}">${x}</option>
						</#list>
					</select>
					
					days and
					
					<select name="amountOfHoursValid">
						<#list 1..24 as x>
							<option value="${x}">${x}</option>
						</#list>
					</select>
					
					hours.
					
					</td>
					<td>
						<input type="submit" value="OK" onclick="document.forms.${screen.name}.__action.value = 'createToken'; document.forms.${screen.name}.submit();">
					</td>
				</tr>
			</table>
		</td>
	</tr>

<#if model.tokens?keys?size gt 0>
	<tr>
		<td>
			<table>
				<tr>
					<td colspan="5">
						<h2>My tokens:</h2>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>
			<table cellpadding="3" border="1" style="width:870px;">
				<tr class="form_listrow0">
					<td>
						<b>Token ID</b>
					</td>
					<td>
						<b>For user</b>
					</td>
					<td>
						<b>Created at</b>
					</td>
					<td>
						<b>Expires at</b>
					</td>
					<td>
						<b>Delete it</b>
					</td>
				</tr>
			<#list model.tokens?keys as k>
				<tr class="form_listrow1">
					<td>
						${k}
					</td>
					<td>
						${model.tokens[k].userName}
					</td>
					<td>
						${model.tokens[k].createdAt?datetime}
					</td>
					<td>
						${model.tokens[k].expiresAt?datetime}
					</td>
					<td align="center">
						<input type="image" src="generated-res/img/exit.bmp" title="Delete token" onclick="document.forms.${screen.name}.__action.value = 'deleteToken_${k}';"/>
					</td>
				</tr>
			</#list>
			</table>
		</td>
	</tr>
</#if>


</table>
<br><br><br>

<#--end of your plugin-->	
			</div>
		</div>
	</div>
</form>
</#macro>
