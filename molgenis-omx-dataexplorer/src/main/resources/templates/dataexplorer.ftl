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
		<script type="text/javascript" src="${resultsTableJavascriptFile}"></script>
		<script type="text/javascript" src="/js/jquery.dynatree.min.js"></script>
		<script type="text/javascript">
			$(function() {
				$("#feature-filters-container").accordion({ collapsible: true });
				window.top.molgenis.fillDataSetSelect(function() {
					<#-- select first dataset -->
					$('#dataset-select option:first').val();
					<#-- fire event handler -->
					$('#dataset-select').change();
					<#-- use chosen plugin for dataset select -->
					$('#dataset-select').chosen();
				});
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span3">
					<div class="well">
						<div class="row-fluid">
							<form class="form-search" onsubmit="return false">
								<div class="input-append">
									<input type="text" id="observationset-search" class="search-query" placeholder="Search data items">
									<button type="submit" class="btn"><i class="icon-large icon-search"></i></button>
								</div>
							</form>
						</div>
						<div class="row-fluid">
							<div id="feature-filters-container">
								<h3>Data item filters</h3>
								<div id="feature-filters"></div>
							</div>
						</div>
						<div class="row-fluid">
							<div id="feature-selection-container">
								<h3>Data item selection</h3>
								<div id="feature-selection"></div>		
							</div>
						</div>			
					</div>		
				</div>
				<div class="span9">
					<div id="dataset-select-container" class="control-group form-horizontal pull-right">
						<div class="controls">
							<label class="control-label" for="dataset-select">Choose a dataset:</label>
							<select data-placeholder="Choose a Dataset" id="dataset-select">
							</select>
						</div>
					</div>
					<div class="row-fluid data-table-container">
						<table id="data-table" class="table table-striped table-condensed"></table>
					</div>
					<div class="row-fluid data-table-header-container">
						<span id="data-table-header"></span>
						<div id="data-table-pager" class="pagination pagination-centered"></div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="feature-filter-dialog"></div>
	</body>
</html>