<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "simple_statistics.js"]>
<@header css js/>
<form id="evaluationForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row">
		<div class="col-md-12">
			<div class="row">
				<div class="col-md-12">
					<legend><center><h3>Ontology Annotator</h3></center></legend>
				</div>
			</div>
			<div class="row">
				<div class="col-md-offset-4 col-md-4 well">
					<div class="row">
						<div class="col-md-offset-1">
							<#if ontologies?? & (ontologies?size > 0)>
							<center><strong>Please select ontologies for annotation:</strong></center>
							<div class="radio">
								<#list ontologies as ontology>
									<label class="checkbox">
										<input name="selectOntologies" type="radio" value="${ontology.identifier}" <#if (ontology_index == 0) >checked</#if>> 
										<a href="${ontology.identifier}" target="_blank">${ontology.name}</a>
									</label>
								</#list>
							</div>
							<#else>
								<center>
									<span>There are no ontologies avaiable!</span>
									<a href="${context_url?replace("ontologyservice","ontologyindexer")}" target="_blank">Please upload new ontologies!</a>
								</center>
							</#if>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-offset-2 col-md-8">
					<div class="fileupload fileupload-new" data-provides="fileupload">
						<div class="group-append">
							<div class="uneditable-input">
								<i class="icon-file fileupload-exists"></i>
								<span class="fileupload-preview"></span>
							</div>
							<span class="btn btn-file btn-info">
								<span class="fileupload-new">Select file</span>
								
								<span class="fileupload-exists">Change</span>
								<input type="file" id="file" name="file" required/>
							</span>
							<a href="#" class="btn btn-danger fileupload-exists" data-dismiss="fileupload">Remove</a>
							<button id="upload-button" type="button" class="btn btn-primary">Upload</button>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-offset-2 col-md-8">
					<center><textarea name="inputTerms" style="width:100%;resize:none" class="form-control" rows="12" placeholder="Please paste a list of terms here. E.g. absent radius"></textarea></center>
				</div>
			</div>
			<br>
			<div class="row">
				<div class="col-md-offset-2 col-md-8">
					<button id="match-button" type="button" class="btn btn-primary">Match</button>
				</div>
			</div>
		</div>
	</div>
</form>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			$('#match-button').click(function(){
				if($('[name="selectOntologies"]').length === 0) {
					molgenis.createAlert([{'message':'There are not ontologies avaiable!'}], 'error');
					return false;
				}
				if($('[name="selectOntologies"]:checked').length === 0) {
					molgenis.createAlert([{'message':'Please select an ontology to match against!'}], 'error');
					return false;
				}
				if($('[name=inputTerms]:eq(0)').val() === ''){
					molgenis.createAlert([{'message':'Please paste the terms you want to match in the text area!'}], 'error');
					return false;
				}
				$('#evaluationForm').attr({
					'action' : molgenis.getContextUrl() + '/match',
					'method' : 'POST'
				}).submit();
			});
			
			$('#upload-button').click(function(){
				if($('#file').val() === '') {
					molgenis.createAlert([{'message' :'Please upload a file that contains a list of terms you like match!'}], 'error');
					return false;
				}
				if($('[name="selectOntologies"]').length === 0) {
					molgenis.createAlert([{'message' :'There are not ontologies avaiable!'}], 'error');
					return false;
				}
				if($('[name="selectOntologies"]:checked').length === 0) {
					molgenis.createAlert([{'message' :'Please select an ontology to match against!'}], 'error');
					return false;
				}
				$('#evaluationForm').attr({
					'action' : molgenis.getContextUrl() + '/match/upload',
					'method' : 'POST'
				}).submit();
			});
		});
	</script>
<@footer/>	