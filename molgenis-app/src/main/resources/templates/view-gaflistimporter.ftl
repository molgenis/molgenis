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
<div class="row">
	<div class="col-md-10 col-md-offset-1 well">
		<legend>Import GAF list</legend>
		
		<div class="row">
			<div class="col-md-6">
				<form role="form" id="gaflist-import-file-form" method="post" action="${context_url?html}${action?html}" enctype="${enctype?html}" onsubmit="parent.showSpinner(function(){$('.modal-body').html('Work in progress..');});  return true;">
					<h4>Import CSV file</h4>
					<#if submit_state?? && submit_state>
						<div class="row">
							<div class="col-md-12">
								<i>File:</i>
								<span>
									<#if fileName?has_content>${fileName?html}</#if>
								</span>
							
								<span class="pull-right">
									<a id="backButton" href="${context_url?url('UTF-8')}" class="btn btn-default">Back</a>
									<input id="submitButton" type="submit" value="Import file" class="btn btn-success"/>
								</span>
							</div>
						</div>
					<#else>
						<div class="row">
							<div class="col-md-12">
								<i>Choose file:</i>
								<span>
									<input type="file" name="csvFile" required/>
								</span>
								
								<span class="pull-right">
									<input id="validateButton" type="submit" value="Validate file" class="btn btn-primary"/>
								</span>
							</div>
						</div>
					</#if>
				</form>
			</div>
		</div>
		
		<div class="row">
			<div class="col-md-12">
				<#if messages?? == true>
					<h4>Import messages:</h4>
					
					<ul>
						<#list messages as message>
							<li>
								<p>${message!}</p>
							</li>
						</#list>
					</ul>
				</#if>
			</div>
		</div>
		
		<div class="row">
			<div class="col-md-12">
				<#if (hasValidationError?? == true) && (hasValidationError == true)>
					<h4>Validation error report</h4>
					${validationReport!}
				</#if>
			</div>
		</div>
	</div>
</div>
<@footer/>
