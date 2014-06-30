<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["gaflistimporter.css"]>
<#assign js=["bootstrap.file-input.js", "gaflistimporter.js"]>
<@header css js/>
<div class="container-fluid">
	<div class="row-fluid">
		<H1>Import GAF list</H1>
	</div>
	<div class="row-fluid">
		<div class="span4">
		<h4>Import CSV file</h4>
			<table class="table">
				<form id="gaflist-import-file-form" method="post" action="${context_url}/import-file" enctype="multipart/form-data">
					<tr>
						<td nowrap><i>Separator:</i></td>
						<td><input type="text" name="separator" class="span2" maxlength="1" value=";"/></td>
						<td></td>
					</tr>
					<tr>
						<td nowrap><i>Choose file:</i></td>
						<td><input type="file" name="csvFile" title="Choose CSV file..."  data-filename-placement="inside"/></td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td><button type="submit" class="btn btn-primary">Submit file</button></td>
						<td></td>
					</tr>
				</form>
			</table>
		</div>
		<div class="span8">
		<#if hasValidationError??>
			<h4>Validation report</h4>
			<#if hasValidationError>
				${validationReport?if_exists}
			<#else>
				No errors found!<br/>
				${importMessage?if_exists}				
			</#if>
		</#if>
		</div>
	</div>
</div>
	
<@footer/>
