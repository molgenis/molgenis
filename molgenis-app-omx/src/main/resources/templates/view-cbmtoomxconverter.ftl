<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
	<div class="row-fluid"
		<form method="post" enctype="multipart/form-data" name="cbmToOmxConverter" action="${context_url}/convert">
		
		<div class="formscreen">
			<div class="screenbody">
				<div class="screenpadding">	
					<h1>Import xml data</h1>
					<i>Upload Xml file with your data</i>
					<br /><br />
					<label for="upload">Please upload the data:</label>
					<input type="file" class="textbox" id="upload" name="upload"/><br /><br />
					<input type="submit" value="Create omx files" id="upload_xml";return true;"/><br />
				</div>
			</div>
		</form>
	</div>
<@footer/>
