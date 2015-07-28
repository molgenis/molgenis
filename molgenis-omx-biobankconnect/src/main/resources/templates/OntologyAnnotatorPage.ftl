<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="well offset3 span6 upper-header">
					<div class="row-fluid">
						<div class="offset1 span4">
							<dt>Ontologies :</dt>
							<dd id="ontology-list"></dd>
							<button id="toggle-select" class="btn btn-link select-all">Deselect all</button>
							<input type="hidden" id="selectedOntologies" name="selectedOntologies" />
						</div>
						<div class="span4">
							<dl>
								<dt>Action :</dt>
								<dd>
									<div class="btn-group">
										<button id="annotate-all-dataitems" class="btn btn-primary">Re-annotate</button>
										<button id="remove-annotations" class="btn">Remove annotates</button>
									</div>
								</dd>
							</dl>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div id="div-info" class="span12 well">	
					<div class="row-fluid">
						<div class="span9"><legend class="legend">Annotate catalogue : <strong><span>${wizard.selectedDataSet.name}</span></strong></legend></div>
						<div  id="div-search" class="span3">
							<div><strong>Search data items :</strong></div>
							<div class="input-append">
								<input id="search-dataitem" type="text" title="Enter your search term" />
								<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span12">
							<div class="data-table-container">
								<table id="dataitem-table" class="table table-striped table-condensed">
								</table>
							</div>
							<div class="pagination pagination-centered">
								<ul></ul>
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
			molgenis.getOntologyAnnotator().changeDataSet('${wizard.selectedDataSet.id?c}');
			molgenis.getOntologyAnnotator().searchOntologies();
			
			$('#toggle-select').click(function(){
				if($(this).hasClass('select-all')){
					$('#ontology-list').find('input').empty().attr('checked', false);
					$(this).removeClass('select-all').addClass('remove-all').empty().append('Select all');
				}else{
					$('#ontology-list').find('input').empty().attr('checked', true);
					$(this).removeClass('remove-all').addClass('select-all').empty().append('Deselect all');
				}
				molgenis.getOntologyAnnotator().searchOntologies();
				return false;
			});
			
			$('#annotate-all-dataitems').click(function(){
				$('#spinner').modal();
				$('#wizardForm').attr({
					'action' : molgenis.getContextUrl() + '/annotate',
					'method' : 'POST'
				}).submit();
			});
			
			$('#remove-annotations').click(function(){
				molgenis.getOntologyAnnotator().confirmation('Confirmation');
				return false;
			});
		});
	</script>
</form>