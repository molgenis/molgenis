<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap-fileupload.min.css", "ontology-indexer.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "ontology-indexer.js"]>
<@header css js/>
	<#if isCorrectOntology?? && !isCorrectOntology> 
		<div class="row-fluid">
			<div class="alert alert-error">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p class="text-error"><strong>Warning!</strong> The file you uploaded is not in OWL or OBO format!</p>
			</div>
		</div>
	<#elseif isIndexRunning?? && isIndexRunning>
		<div class="row-fluid">
			<div class="alert alert-info">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<strong>Message : </strong> ontology is being processed, please be patient. Click on refresh to check the status of index.
			</div>
		</div>
	<#elseif isCorrectZipFile?? && !isCorrectZipFile>
		<div class="row-fluid">
			<div class="alert alert-error">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p class="text-error"><strong>Message : </strong> ${message}</p>
			</div>
		</div>
	<#elseif removeSuccess??>
		<div class="row-fluid">
			<div <#if removeSuccess>class="alert alert-info"<#else>class="alert alert-error</#if>">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p><strong>Message : </strong> ${message}</p>
			</div>
		</div>
	</#if>
	<form id="ontologyindexer-form" class="form-horizontal" enctype="multipart/form-data">
		<div class="row-fluid">
			<div class="span12">
				<legend>Add new ontologies</legend>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<input type="hidden" name="ontologyUri"/>
				<div class="row-fluid">
					<div id="div-index-ontology" class="span8 well">
						<div class="row-fluid">
							<div class="span12">
								<table id="ontology-table" class="table table-striped table-bordered">
									<tr>
										<th>
											Ontology
										</th>
										<th>
											Ontology uri
										</th>
										<th>
											Status
										</th>
										<th>
											Remove
										</th>
									</tr>
								</table>
							</div>
						</div>
						<div id="div-new-ontology" class="row-fluid">
							<div class="span12">
								<div>
									<p><strong>1. Please name the ontology</strong></p>
									<input id="ontologyName" name="ontologyName" type="text" /> 
								</div>
								<p><strong>2. Please upload ontology file</strong></p>
								<div class="fileupload fileupload-new" data-provides="fileupload">
									<div class="input-append">
										<div class="uneditable-input">
											<i class="icon-file fileupload-exists"></i>
											<span class="fileupload-preview"></span>
										</div>
										<span class="btn btn-file btn-info">
											<span class="fileupload-new">Select file</span>
											<span class="fileupload-exists">Change</span>
											<input type="file" id="uploadedOntology" name="file" required/>
										</span>
										<a href="#" class="btn btn-danger fileupload-exists" data-dismiss="fileupload">Remove</a>
									</div>
								</div>
								<div class="btn-group">
									<button class="btn btn-primary" id="index-button" type="button">Start index</button>
									<button class="btn" id="refresh-button" type="button">Refresh</button>
								</div>
							</div>
						</div>
					</div>
					<div class="span4">
						<div class="accordion-group">
						    <div class="accordion-heading">
								<h5 class="text-left text-info">Actions</h5>	
							</div>
							<div class="accordion-body in">
								<div class="align-list">Table <strong>above</strong> shows all avaiable indexed ontologies in BiobankConnect.</div>
								<ol class="action-list">
									<li>
										Enter <strong>name</strong> of ontology (compulsory).
									</li>
									<li>
										Uplaod ontology <strong>zip</strong> file (OWL/OBO).
									</li>
									<li>
										Click on <strong>'Start index'</strong> to start indexing.
									</li>
									<li>
										Click on <strong>'Refresh'</strong> to check state of indexing.
									</li>
									<li>
										<strong>Hint</strong> : the size of uploaded file cannot exceed <strong>25MB.</strong>
									</li>
								</ol>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</form>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			var ontologyUri = null;
			<#if ontologyUri ??> 
				ontologyUri = "${ontologyUri}"; 
			</#if>
			molgenis.searchAvailableIndices(ontologyUri);
			<#if isIndexRunning ?? && isIndexRunning>
				$('#index-button').attr('disabled','disabled');
			</#if>
		});
	</script>
<@footer/>