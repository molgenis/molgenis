<!DOCTYPE html>
<html>
	<head>
		<title>Dataset deletion plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/datasetdeleter.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript">
			$(function() {
				window.top.molgenis.fillDataSetSelect(function() {
					<#-- select first dataset -->
					$('#dataset-select option:first').val();
					<#-- fire event handler -->
					$('#dataset-select').change();
					<#-- use chosen plugin for dataset select -->
					$('#dataset-select').chosen();
				});
				
				var submitBtn = $('#delete-button');
				var form = $('#deletedataset-form');
				form.submit(function(e){
					window.top.molgenis.deleteDataSet(e);
				});
				
				submitBtn.click(function(e) {
					e.preventDefault();
					e.stopPropagation();
					form.submit();
				});
			});		
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span9">
					<h4>Delete dataset:</h4>
					<div class="well" style="width: 400px; max-height:400px; overflow:auto">
						<form id="deletedataset-form" enctype="multipart/form-data">
						<label class="control-label" for="dataset-select">Choose a dataset:</label>
						<select data-placeholder="Choose a Dataset to delete" id="dataset-select" name="dataset">
						</select><br>
						<input type="checkbox" name="deletemetadata" checked> Delete the metadata of this dataset.
					</form>
					</div>
					<a id="delete-button" class="btn">Delete</a>
				</div>
			</div>
		</div>		
	</body>
</html>