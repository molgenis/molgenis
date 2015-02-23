<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap-fileupload.min.css", "ontology-indexer.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "ontology-indexer.js"]>
<@header css js/>
	<#if isCorrectOntology?? && !isCorrectOntology> 
		<div class="row">
			<div class="alert alert-danger">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p class="text-error"><strong>Warning!</strong> The file you uploaded is not in OWL or OBO format!</p>
			</div>
		</div>
	<#elseif isIndexRunning?? && isIndexRunning>
		<div class="row">
			<div class="alert alert-info">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<strong>Message : </strong> ontology is being processed, please be patient. Click on refresh to check the status of index.
			</div>
		</div>
	<#elseif isCorrectZipFile?? && !isCorrectZipFile>
		<div class="row">
			<div class="alert alert-danger">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p class="text-error"><strong>Message : </strong> ${message?html}</p>
			</div>
		</div>
	<#elseif removeSuccess??>
		<div class="row">
			<div <#if removeSuccess>class="alert alert-info"<#else>class="alert alert-danger</#if>">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p><strong>Message : </strong> ${message?html}</p>
			</div>
		</div>
	</#if>
	<form id="ontologyindexer-form" class="form-horizontal" enctype="multipart/form-data">
		<br>
		<div class="row">
			<div class="col-md-12 well custom-white-well">
				<div class="row">
					<div class="col-md-offset-3 col-md-6 text-align-center">
						<legend class="custom-purple-legend">
							Add new ontologies
						</legend>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<input type="hidden" name="ontologyUri"/>
						<div class="row">
							<div id="div-index-ontology" class="col-md-8">
								<div class="row">
									<div class="col-md-12">
										<table id="ontology-table" class="table table-bordered">
											<tr>
												<th>Ontology</th>
												<th>Ontology uri</th>
												<th>Status</th>
												<th>Remove</th>
											</tr>
										</table>
									</div>
								</div>
								<div id="div-new-ontology" class="row">
									<div class="col-md-12">
										<div class="row">
											<div class="col-md-4">
												<p><strong>1. Please name the ontology</strong></p>
												<input id="ontologyName" name="ontologyName" type="text" class="form-control"/>
											</div>
											<div class="col-md-8">
												<div class="float-right"><a href="/html/custom_ontology.owl.zip">Download example ontology</a></div>
											</div>	 
										</div>
										<p><strong>2. Please upload ontology file</strong></p>
										<div class="fileupload fileupload-new" data-provides="fileupload">
											<div class="group-append">
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
											<button class="btn btn-default" id="refresh-button" type="button">Refresh</button>
										</div>
									</div>
								</div>
							</div>
							<div class="col-md-4">
								<div class="panel panel-primary">
								    <div class="panel-heading">
										<h5 class="panel-title">Actions</h5>	
									</div>
									<div class="panel-body">
										<div class="row">
											<div class="col-md-offset-1 col-md-10">
												<div class="align-list">Table <strong>above</strong> shows all avaiable indexed ontologies in BiobankConnect.</div>
												<ol class="action-list">
													<li>
														Enter <strong>name</strong> of ontology (compulsory).
													</li>
													<li>
														Upload ontology <strong>zip</strong> file (OWL/OBO).
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
				ontologyUri = "${ontologyUri?js_string}"; 
			</#if>
			molgenis.searchAvailableIndices(ontologyUri);
			<#if isIndexRunning ?? && isIndexRunning>
				$('#index-button').attr('disabled','disabled');
			</#if>
		});
	</script>
<@footer/>