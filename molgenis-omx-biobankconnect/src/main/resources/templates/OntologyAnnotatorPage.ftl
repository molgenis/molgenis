<script src="<@resource_href "/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/ontology-annotator.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-select/bootstrap-select.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-typeahead/typeahead.bundle.min.js"/>"></script>
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/ontology-annotator.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div id="div-info" class="col-md-12 well custom-white-well">
		<div class="row">
			<div class="col-md-offset-3 col-md-6 upper-header">
				<div class="text-align-center"><legend class="custom-purple-legend">Annotate catalogue <strong><span>${wizard.selectedDataSet.name}</span></strong></legend></div>
			</div>
		</div>
		<div class="row">
			<div class="well col-md-offset-3 col-md-6 upper-header">
			<div class="col-md-offset-1 col-md-4">
				<dt>Ontologies :</dt>
				<dd id="ontology-list"></dd>
				<button id="toggle-select" class="btn btn-link select-all">Deselect all</button>
				<input type="hidden" id="selectedOntologies" name="selectedOntologies" />
			</div>
			<div class="col-md-offset-1 col-md-6">
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
		<div class="row">
			<div id="div-search" class="col-md-3">
				<div><strong>Search data items :</strong></div>
				<div class="input-group">
					<input id="search-dataitem" class="form-control" title="Enter your search term" style="border-top-left-radius:5px;border-bottom-left-radius:5px;"/>
					<div id="search-button" class="input-group-addon"><span class="glyphicon glyphicon-search"></span></div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-4">
				Number of data items : <span id="dataitem-number"></span>
			</div>
		</div><br>
		<div class="row">
			<div class="col-md-12">
				<div id="container" class="data-table-container">
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