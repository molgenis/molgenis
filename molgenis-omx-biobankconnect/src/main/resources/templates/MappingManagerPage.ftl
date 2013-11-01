<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<legend>Curate mappings for : <strong>${wizard.selectedDataSet.name}</strong></legend>
	<div class="row-fluid">
		<div  id="div-search" class="span12">
			<div><strong>Search data items :</strong></div>
			<div class="input-append row-fluid">
				<div class="span3">
					<input id="search-dataitem" type="text" title="Enter your search term" />
					<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
				</div>
				<div class="span1">
					<button id="downloadButton" class="btn btn-primary">Download</button>
				</div>
				<div class="offset7 span1">
					<a id="help-button" class="btn">help <i class="icon-question-sign icon-large"></i></a>
				</div>
			</div>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span12">
					<div id="data-table-container" class="row-fluid data-table-container">
						<table id="dataitem-table" class="table table-striped table-condensed show-border">
						</table>
					</div>
					<div class="pagination pagination-centered">
						<ul id="table-papger"></ul>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.getMappingManager().changeDataSet(${wizard.selectedDataSet.id?c});
			$('#downloadButton').click(function(){
				molgenis.getMappingManager().downloadMappings();
				return false;
			});
			$('#help-button').click(function(){
				molgenis.getMappingManager().createHelpModal();
			}).click();
		});
	</script>
</form>