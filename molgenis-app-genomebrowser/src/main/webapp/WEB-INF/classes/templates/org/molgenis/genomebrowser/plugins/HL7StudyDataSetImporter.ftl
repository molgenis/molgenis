<#macro HL7StudyDataSetImporter screen>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" id="${screen.name}" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action" value="">

	<div class="formscreen">
		<div class="form_header" id="${screen.name}">
			${screen.label}
		</div>
	<#-- optional: mechanism to show messages -->
	<#list screen.getMessages() as message>
		<#if message.success>
		<p class="successmessage">${message.text}</p>
		<#else>
		<p class="errormessage">${message.text}</p>
		</#if>
	</#list>
		<div class="screenbody" id="container-plugin">
			<div class="screenpadding">
				<p>Import a LifeLines study dataset (HL7 XML file):</p>
				<div class="fileupload fileupload-new row-fluid grid" data-provides="fileupload">
					<div class="input-append">
						<div class="uneditable-input span3"><i class="icon-file fileupload-exists"></i> <span class="fileupload-preview"></span></div><span class="btn btn-file"><span class="fileupload-new">Select file</span><span class="fileupload-exists">Change</span><input type="file" name="file" /></span><a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Remove</a><button class="btn import-btn">Import</button>
					</div>
				</div>
				<script type="text/javascript">
					$('.import-btn').click(function(e) {
						$('input[name=__action]').val('doImport');
						$('#${screen.name}').submit();
					});
				</script>
			</div>
		</div>
	</div>
</form>
</#macro>
