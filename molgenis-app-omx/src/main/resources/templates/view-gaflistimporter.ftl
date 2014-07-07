<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["gaflistimporter.css"]>
<#assign js=[]>
<@header css js/>
<div class="container-fluid">
	<div class="well">
		<div class="row-fluid">
			<H2>Import GAF list</H2>
		</div>
		<div class="row-fluid">	
			<div class="span4">
				<form id="gaflist-import-file-form" method="post" action="${context_url}" enctype="multipart/form-data" onsubmit="parent.showSpinner(function(){$('.modal-body').html('Validating and importing GAF list');});  return true;">
					<h4>Import CSV file</h4>
					<table class="table">
						<tbody>
							<tr>
								<td nowrap><i>Separator:</i></td>
								<td><input type="text" name="separator" class="span2" maxlength="1" value=""/></td>
							</tr>
							<tr>
								<td nowrap><i>Choose file:</i></td>
								<td><input type="file" name="csvFile" required/></td>
							</tr>
							<tr>
								<td></td>
								<td><input id="submitButton" type="submit" value="Submit file" class="btn btn-primary" /></td>
							</tr>
						</tbody>
					</table>
				</form>
			</div>
			
			<div class="span8">
				<#if messages?? == true>
					<h4>Import messages:</h4>
					<ul>
					<#list messages as message>
						<li>
							<p>${message?if_exists}</p>
						</li>
					</#list>
					</ul>
				</#if>
				<#if (hasValidationError?? == true) && (hasValidationError == true)>
					<h4>Validation report</h4>
					${validationReport?if_exists}
				</#if>
			</div>
		</div>
	</div>
</div>
<@footer/>
