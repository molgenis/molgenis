<#macro plugin_topmenu_LoginScreen screen>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}"" />
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action" />
	
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
		
		<div class="screenbody">
			<div class="screenpadding">	
<#--begin your plugin-->	

<#assign login = screen.login/>
<#if login.authenticated>
	You are logged in as '${login.userName}'.
</#if>
	<table>
<#list screen.inputs as input>
	<tr><td><label>${input.label}</label></td><td>${input.toHtml()}</td></tr>
</#list> 
	</table>


<#--end of your plugin-->	
			</div>
		</div>
	</div>
</form>
</#macro>
