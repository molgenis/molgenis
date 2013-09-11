<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
	<div class="row-fluid">
		<h3>Convert CBM XML data to OMX data</h3>
	</div>
	<div class="row-fluid">
		<form class="form-horizontal" method="post" enctype="multipart/form-data" name="cbmToOmxConverter" action="${context_url}/convert">
			<div class="control-group">
				<label class="control-label" for="upload">Please upload the data:</label>
				<div class="controls">
					<input type="file" id="upload" name="upload" required>
				</div>
			</div>
			<button id="upload_xml" type="submit" class="btn btn-default">Convert</button>
		</form>
	</div>
<@footer/>
