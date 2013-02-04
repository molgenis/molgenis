<!-- org.molgenis.framework.ui.MolgenisForm -->
<form method="post" enctype="multipart/form-data" id="${screen.name}" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}"/>
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<@hidden name="__action"/>
	
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
    
    <#-- navigate to the EasyPluginView.getInputs(model)-->
<#if content?exists>${content}</#if>
     
 <#--end of your plugin-->	
			</div>
		</div>
	</div>
</form>
<script>
$("#${screen.name}").validate();
</script>