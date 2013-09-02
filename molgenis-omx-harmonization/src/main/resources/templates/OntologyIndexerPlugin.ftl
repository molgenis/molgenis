<!DOCTYPE html>
<html>
	<head>
		<title>Data indexer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-datetimepicker.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-fileupload.min.css" type="text/css">
		<link rel="stylesheet" href="/css/ontology-annotator.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/common-component.js"></script>
		<script type="text/javascript" src="/js/bootstrap-fileupload.min.js"></script>
		<script type="text/javascript" src="/js/ontology-indexer.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				var molgenis = window.top.molgenis;
				$(function(){
					var ontologyUri = null;
					<#if ontologyUri ??> 
						ontologyUri = "${ontologyUri}"; 
					</#if>
					molgenis.searchAvailableIndices(ontologyUri);
					
					<#if isIndexRunning ?? && isIndexRunning>
						$('#index-button').attr('disabled','disabled');
					</#if>
				});
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div id="alertMessage">
				<#if isCorrectOntology?? && !isCorrectOntology> 
					<div class="alert alert-error">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
				  		<p class="text-error"><strong>Warning!</strong> The file you uploaded is not in OWL or OBO format!</p>
					</div>
				<#elseif isIndexRunning?? && isIndexRunning>
					<div class="alert">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
				  		<strong>Message : </strong> ontology is being processed, please be patient. Click on refresh to check the status of index.
					</div>
				<#elseif isCorrectZipFile?? && !isCorrectZipFile>
					<div class="alert alert-error">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
				  		<p class="text-error"><strong>Message : </strong> ${message}</p>
					</div>
				</#if>
				</div>
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
								<form id="ontologyindexer-form" class="form-horizontal" enctype="multipart/form-data">
									<h1>Index new ontologies</h1>
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
									<button class="btn btn-primary" id="index-button" type="button">Start index</button>
									<button class="btn btn-primary" id="refresh-button" type="button">Refresh</button>
								</form>
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
	</body>
</html>