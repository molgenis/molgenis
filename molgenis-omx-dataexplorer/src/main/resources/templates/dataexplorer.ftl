<!DOCTYPE html>
<html>
	<head>
		<title>Data explorer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/dataexplorer.css" type="text/css">
		<link rel="stylesheet" href="/css/ui.dynatree.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/dataexplorer.js"></script>
		<script type="text/javascript" src="/js/jquery.dynatree.min.js"></script>
		<script type="text/javascript">
			$(function() {
				$("#feature-filters").accordion({ collapsible: true });
				window.molgenis.fillDataSetSelect(function() {
					$("#feature-selection").accordion({ collapsible: true });
					<#-- select first dataset -->
					$('#dataset-select option:first').val();
					<#-- fire event handler -->
					$('#dataset-select').change();
					<#-- use chosen plugin for dataset select -->
					$('#dataset-select').chosen();
					<#-- use dynatree plugin for feature selection -->
					//$("#feature-selection").dynatree({checkbox: true});
				});
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span3">
					<div class="row-fluid">
						<label id ="dataset-select-label">Choose a dataset:</label>
						<select data-placeholder="Choose a Dataset" id="dataset-select">
						</select>
					</div>
					<div class="well">
					<div class="row-fluid">
						<div id="feature-filters">
							<h3>Data item filters</h3>
						</div>
					</div>
					<div class="row-fluid">
						<div id="feature-selection">
						</div>
					</div>			
					</div>		
				</div>
				<div class="span9">
					<form class="form-search" onsubmit="return false">
						<div class="input-append">
							<input type="text" id="observationset-search" class="search-query offset3" placeholder="Search data items">
							<button type="submit" class="btn">Search</button>
						</div>
					</form>
					<div class="row-fluid data-table-container">
						<table id="data-table" class="table table-striped table-condensed">
						</table>
					</div>
					<div class="row-fluid data-table-header-container">
						<span id="data-table-header"></span>
						<div id="data-table-pager" class="pagination pagination-centered"></div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>