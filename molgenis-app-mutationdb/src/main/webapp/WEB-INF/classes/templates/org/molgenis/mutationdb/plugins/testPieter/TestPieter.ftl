<#macro TestPieter screen>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action">
	
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
		
			<p>${screen.testString1}</p>
		
			<#if screen.listOfNames??>
				<select name="listje">
				<#list screen.listOfNames as name>
					<option value="${name}">${name}</option>
				${name}
				
				</#list>
				</select>
			
			<form action="${screen.setTestString2('string2')}" method="post" id="TestField" name="TestField">
				<label style="width:16em;float:left;" for="testinput">Input</label>
				<input type="text" id="string2" class="" name="string2">
				<button type="submit" class="btn btn-small" id="submit">submit</button>
			</form>
			<p>${screen.getTestString2()}</p>
			
			</#if>
		</div>
	</div>
</form>
</#macro>
