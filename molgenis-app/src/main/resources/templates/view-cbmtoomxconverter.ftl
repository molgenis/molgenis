<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
	<div class="row">
		<h3>Convert CBM XML data to OMX data</h3>
	</div>
	<div class="row">
		<form class="form-horizontal" method="post" enctype="multipart/form-data" name="cbmToOmxConverter" action="${context_url}/convert">
			<div class="form-group">
				<label class="col-md-3 control-label" for="upload">Please upload the data:</label>
				<div class="col-md-9">
					<input type="file" id="upload" name="upload" required>
				</div>
			</div>
			<button id="upload_xml" type="submit" class="btn btn-default">Convert</button>
		</form>
	</div>
<@footer/>
