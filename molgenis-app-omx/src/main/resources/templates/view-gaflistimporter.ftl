<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[
	"gaflistimporter.css",
	"jquery.bootstrap.wizard.css",
	"jquery-ui-1.9.2.custom.min.css",
	"jquery.molgenis.table.css"]>
<#assign js=[
	"jquery-ui-1.9.2.custom.min.js",
	"jquery.bootstrap.wizard.min.js",
	"jquery.molgenis.table.js"]>	
<@header css js/>
<div class="container-fluid">
	<div class="well">
		<div class="row-fluid">
			<H2>Import GAF list</H2>
		</div>
		<div class="row-fluid">	
			<div class="span6">
				<form id="gaflist-import-file-form" method="post" action="${context_url}${action}" enctype="${enctype}" onsubmit="parent.showSpinner(function(){$('.modal-body').html('Work in progress..');});  return true;">
					<h4>Import CSV file</h4>
					<table class="table">
						<tbody>
							<#if submit_state?? && submit_state>
								<tr>
									<td>File: </td>
									<td><#if fileName?has_content>${fileName}</#if></td>
									<td><a id="backButton" href="${context_url}" class="btn">Back</a>
									<input id="submitButton" type="submit" value="Import file" class="btn btn-success"/></td>
								</tr>
							<#else>
								<tr>
									<td nowrap><i>Choose file:</i></td>
									<td><input type="file" name="csvFile" required/></td>
									<td><input id="validateButton" type="submit" value="Validate file" class="btn btn-primary"/></td>
								</tr>
							</#if>
						</tbody>
					</table>
				</form>
			</div>
		</div>
		<div class="row-fluid">	
			<div class="span12">
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
			</div>
		</div>
		<div class="row-fluid">	
			<div class="span12">		
				<#if (hasValidationError?? == true) && (hasValidationError == true)>
					<h4>Validation error report</h4>
					${validationReport?if_exists}
				</#if>
			</div>
		</div>
	</div>
</div>
<@footer/>
