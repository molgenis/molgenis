<script src="<@resource_href "/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/ontology-annotator.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/ontology-annotator.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div class="row">
		<div class="col-md-12">
			<div class="row">
				<div class="well col-md-offset-3 col-md-6 upper-header">
					<div class="row">
						<div class="col-md-offset-1 col-md-4">
							<dt>Ontologies :</dt>
							<dd id="ontology-list"></dd>
							<button id="toggle-select" class="btn btn-link select-all">Deselect all</button>
							<input type="hidden" id="selectedOntologies" name="selectedOntologies" />
						</div>
						<div class="col-md-4">
							<dl>
								<dt>Action :</dt>
								<dd>
									<div class="btn-group">
										<button id="annotate-all-dataitems" class="btn btn-primary">Re-annotate</button>
										<button id="remove-annotations" class="btn btn-default">Remove annotates</button>
									</div>
								</dd>
							</dl>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div id="div-info" class="col-md-12 well custom-white-well">	
					<br>
					<div class="row">
						<div class="span9"><legend class="legend">Annotate catalogue <strong><span>${wizard.selectedDataSet.name}</span></strong></legend></div>
						<div  id="div-search" class="col-md-3">
							<div><strong>Search data items :</strong></div>
							<div class="group-append">
								<input id="search-dataitem" type="text" title="Enter your search term" />
								<button class="btn btn-default" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-md-4">
							Number of data items : <span id="dataitem-number"></span>
						</div>
					</div>
					<div class="row">
						<div class="col-md-12">
							<div id="container" class="data-table-container">
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.ontologyMatcherRunning(function()
			{
				var ontologyAnnotator = new molgenis.OntologyAnnotator();
				ontologyAnnotator.changeDataSet('${wizard.selectedDataSet.id?c}');
				ontologyAnnotator.searchOntologies($('#ontology-list'), $('#selectedOntologies'));
				
				$('#toggle-select').click(function(){
					
					if($(this).hasClass('select-all')){
						$('#ontology-list').find('input').empty().attr('checked', false);
						$(this).removeClass('select-all').addClass('remove-all').empty().append('Select all');
					}else{
						$('#ontology-list').find('input').empty().attr('checked', true);
						$(this).removeClass('remove-all').addClass('select-all').empty().append('Deselect all');
					}
					ontologyAnnotator.searchOntologies($('#ontology-list'), $('#selectedOntologies'));
					return false;
				});
				
				$('#annotate-all-dataitems').click(function(){
					ontologyAnnotator.annotateConfirmation('Warning', true);
					return false;
				});
				
				$('#remove-annotations').click(function(){
					ontologyAnnotator.annotateConfirmation('Warning', false);
					return false;
				});
			});
		});
	</script>
</form>