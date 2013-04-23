<#macro ImportWorkflowPlugin screen>
	<#assign form = screen.name>
	
	<form method="post" enctype="multipart/form-data" name="${form}" action="">
		<!--needed in every form: to redirect the request to the right screen-->
		<input type="hidden" name="__target" value="${screen.name}">
		<!--needed in every form: to define the action. This can be set by the submit button-->
		<input type="hidden" name="__action">
		<!--need to be set to "true" in order to force a download-->
		<input type="hidden" name="__show">
		
		<div class="formscreen"">
			<div class="form_header" id="${screen.getName()}">${screen.label}</div>

			<div style="padding-top: 25px">
				<input type="file" name="zip"style="height:25px" />
				<input type="submit" value="upload" />
			</div>
		</div>
	</form>
</#macro>