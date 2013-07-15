<#macro OntologyIndexerPlugin screen>
<#assign model = screen.myModel>
	<script>
		$(function(){
			var ontologyUri = null;
			<#if model.ontologyUri ??> 
				ontologyUri = "${model.ontologyUri}"; 
			</#if>
			molgenis.searchAvailableIndices(ontologyUri);
			
			var isRunningIndex = false;
			<#if model.isStartProcess()>
				isRunningIndex = true;
			</#if>
			if(isRunningIndex){
				$('#index-button').attr('disabled','disabled');
			}
		});
	</script>
<form method="post" id="harmonizationIndexer-form" name="${screen.name}" enctype="multipart/form-data" action="molgenis.do">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<input type="hidden" name="__action">
	<div class="formscreen">
		<div class="form_header" id="${screen.name}">
			${screen.label}
		</div>
		<div id="alertMessage">
		<#if !model.isCorrectOntology()> 
			<div class="alert alert-error">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<p class="text-error"><strong>Warning!</strong> The file you uploaded is not in OWL or OBO format!</p>
			</div>
		<#elseif model.isStartProcess()>
			<div class="alert">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<strong>Message : </strong> ontology is being processed, please be patient. Click on refresh to check the status of index.
			</div>
		</#if>
		</div>
		<div class="screenbody">
			<div class="container-fluid">
				<div class="row-fluid">
					<div class="span12">
						<div class="span6">
							<div class="row-fluid">
								<div class="span12">
									<h1>Ontologies</h1>
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
										</tr>
									</table>
								</div>
							</div>
							<div class="row-fluid">
								<div class="span12">
									<h1>Index new ontologies</h1>
									<div>
										<p><strong>1. Please name the ontology</strong></p>
										<input id="ontologyName" name="ontologyName" type="text" /> 
									</div>
									<p><strong>2. Please uploda ontology file</strong></p>
									<div class="fileupload fileupload-new" data-provides="fileupload">
										<div class="input-append">
											<div class="uneditable-input">
												<i class="icon-file fileupload-exists"></i>
												<span class="fileupload-preview"></span>
											</div>
											<span class="btn btn-file btn-info">
												<span class="fileupload-new">Select file</span>
												<span class="fileupload-exists">Change</span>
												<input type="file" id="uploadedOntology" name="uploadedOntology" />
											</span>
											<a href="#" class="btn btn-danger fileupload-exists" data-dismiss="fileupload">Remove</a>
										</div>
									</div>
									<button class="btn btn-primary" id="index-button" type="button">Start index</button>
									<button class="btn btn-primary" id="refresh-button" type="button">Refresh</button>
								</div>
							</div>
						</div>
						<div class="span5">
							<div class="accordion-group">
							    <div class="accordion-heading">
									<h5 class="text-left text-info">Help!</h5>	
								</div>
								<div class="accordion-body collapse in">
									<p class="justify-text">Ontologies are used to expand semantics of data items so that those data items annotated with 
									ontologies terms would have more synonyms and relevent terms that would be more likely matched with 
									other catalogue data items. Please index the ontologies in advance. It will largely speed up the matching process.
									</p>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>
</#macro>