<form id="mappingmanager-form" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span4">
					<h3>Step4 : Curate mappings</h3>
				</div>
			</div>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<button id="downloadButton" class="btn btn-primary float-right">Download</button>
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
			molgenis.changeDataSet(${selectedDataSet.id?c});
			$('#downloadButton').click(function(){
				molgenis.downloadMappings();
				return false;
			});
		});
	</script>
</form>