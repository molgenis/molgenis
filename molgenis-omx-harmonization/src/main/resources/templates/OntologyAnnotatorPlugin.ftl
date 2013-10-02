<form id="ontologyannotator-form" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span4">
					<h3 class="catalogue-chooser-header">Step2 : Annotate data items</h3>
				</div>
			</div>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="well span6 upper-header">
					<div class="span12">
						<dl>
							<dt>Action :</dt>
							<dd>
								<div class="btn-group">
									<button id="annotate-all-dataitems" class="btn">Annotate with all</button>
									<button id="annotate-dataitems" class="btn">Annotate</button>
								</div>
							</dd>
						</dl>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div id="div-info" class="span12 well">	
					<div class="row-fluid">
						<div class="span9"><legend class="legend">Annotate catalogue : <strong><span>${selectedDataSet.name}</span></strong></legend></div>
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
			molgenis.getOntologyAnnotator().changeDataSet('${selectedDataSet.id?c}');
		});
	</script>
</form>