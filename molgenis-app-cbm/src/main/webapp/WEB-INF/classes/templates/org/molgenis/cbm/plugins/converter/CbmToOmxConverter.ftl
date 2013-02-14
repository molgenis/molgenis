<#macro plugins_converter_CbmToOmxConverter screen>
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
			<h1>Import xml data</h1>
			<i>Upload Xml file with your data</i>
			<br /><br />
			<label for="upload">Please upload the data:</label>
			<input type="file" class="textbox" id="upload" name="upload"/><br /><br />
			
			<input type="submit" value="Create omx files" id="upload_xml" onclick="__action.value='upload';return true;"/><br />
			
		</div>
	</div>
</form>
</#macro>
