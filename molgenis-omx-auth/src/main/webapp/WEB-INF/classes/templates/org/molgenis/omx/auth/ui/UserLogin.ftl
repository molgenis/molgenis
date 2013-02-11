<form action="molgenis.do" method="post" enctype="multipart/form-data" id="${screen.name}" name="${screen.name}">
	<input type="hidden" name="__target" value="${screen.name}"/>
	<input type="hidden" name="select" value="${screen.name}"/>
	<input type="hidden" name="__action" value="Login"/>
	<input type="hidden" name="op" value=""/>
	<input type="hidden" name="__show" value=""/>

	<div class="formscreen">
		<div class="form_header" id="${screen.getName()}">
			${screen.label}
		</div>
		<#--messages-->
		<#list screen.getMessages() as message>
			<#if message.text??>
				<#if message.success>
					<p class="successmessage">${message.text}</p>
				<#else>
					<p class="errormessage">${message.text}</p>
				</#if>
			</#if>
		</#list>
		<div class="screenbody">
			<div class="screenpadding">	

				<#assign login = model.getController().getApplicationController().getLogin()/>
				
				<#if model.action == "Register">
				
					<#include "register.ftl">
				
				<#elseif model.action == "Forgot">
				
					<#include "forgot.ftl">
				
				<#elseif login.authenticated>
				
					<#include "userarea.ftl">
				
				<#else>
				
					<#include "authenticate.ftl">
				
				</#if>

			</div>
		</div>
	</div>
</form>
<script>
$("#${screen.name}").validate();
</script>