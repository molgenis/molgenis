<!DOCTYPE html>
<html>
	<head>
		<title>Index plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-fileupload.min.css" type="text/css">
		<link rel="stylesheet" href="/css/harmonization-indexer.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/harmonization-indexer.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap-fileupload.min.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span12">
					<div class="span6">
						<div class="row-fluid">
							<div class="span12">
								<h1>Ontologies</h1>
								<table class="table table-striped table-bordered">
									<tr>
										<th>
											Ontology
										</th>
										<th>
											Status
										</th>
									</tr>
									<tr>
										<td>
											NCI Thesaurus
										</td>
										<td>
											Indexed
										</td>
									</tr>
									<tr>
										<td>
											SNOMED CT
										</td>
										<td>
											Being indexed...
										</td>
									</tr>
								</table>
							</div>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<h1>Index new ontologies</h1>	
								<div class="fileupload fileupload-new" data-provides="fileupload">
									<div class="input-append">
										<div class="uneditable-input">
											<i class="icon-file fileupload-exists"></i>
											<span class="fileupload-preview"></span>
										</div>
										<span class="btn btn-file btn-info">
											<span class="fileupload-new">Select file</span>
											<span class="fileupload-exists">Change</span>
											<input type="file" id="uploadedOntology" name="uploadedOntology"/>
										</span>
										<a href="#" class="btn btn-danger fileupload-exists" data-dismiss="fileupload">Remove</a>
									</div>
								</div>
								<button class="btn btn-primary" type="button">Start index</button>
								<button class="btn btn-primary" type="button">Refresh</button>
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