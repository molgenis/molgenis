<#if enable_spring_ui>
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css", "bootstrap-datetimepicker.min.css", "dataexplorer.css", "ui.dynatree.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "chosen.jquery.min.js", "dataexplorer.js", "${resultsTableJavascriptFile}", "jquery.dynatree.min.js", "bootstrap-datetimepicker.min.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span3">
			<div class="well">
				<div class="row-fluid">
					<#-- add span12 to ensure that input is styled correctly at low and high solutions -->
					<div class="input-append span12" id="observationset-search-container">
						<#-- add span11 to ensure that input is styled correctly at low and high solutions -->
						<input class="span10" id="observationset-search" type="text" placeholder="Search data values">
						<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
					</div>
				</div>
				<div class="row-fluid">
					<div class="accordion" id="feature-filters-container">
						<div class="accordion-group">
						    <div class="accordion-heading">
								<a class="accordion-toggle" data-toggle="false" data-parent="#feature-filters-container" href="#feature-filters">Data item filters</a>
							</div>
							<div class="accordion-body collapse in">
								<div class="accordion-inner" id="feature-filters"></div>
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="accordion" id="feature-selection-container">
						<div class="accordion-group">
						    <div class="accordion-heading">
								<a class="accordion-toggle" data-toggle="false" data-parent="#feature-selection-container" href="#feature-selection">Data item selection</a>
							</div>
							<div class="accordion-body collapse in">
								<div class="accordion-inner" id="feature-selection"></div>
							</div>
						</div>
					</div>
				</div>			
			</div>		
		</div>
		<div class="span9">
			<div id="dataset-select-container" class="control-group form-horizontal">
				<div id="data-table-header" class="pull-left"></div>
				<div class="controls pull-right">
					<label class="control-label" for="dataset-select">Choose a dataset:</label>
					<select data-placeholder="Choose a Dataset" id="dataset-select">
				<#list dataSets as dataSet>
					<option value="/api/v1/dataset/${dataSet.id?c}"<#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
				</#list>
					</select>
				</div>
			</div>
			<div class="row-fluid data-table-container">
				<table id="data-table" class="table table-striped table-condensed"></table>
			</div>
			<div class="row-fluid data-table-pager-container">
				<a id="download-button" class="btn" href="#">Download as csv</a>
				<div id="data-table-pager" class="pagination pagination-centered"></div>
			</div>
		</div>
		<div class="feature-filter-dialog"></div>	
	</div>
<@footer/>
<#else>
<!DOCTYPE html>
<html>
	<head>
		<title>Data explorer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-datetimepicker.min.css" type="text/css">
		<link rel="stylesheet" href="/css/dataexplorer.css" type="text/css">
		<link rel="stylesheet" href="/css/ui.dynatree.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/dataexplorer.js"></script>
		<script type="text/javascript" src="/js/${resultsTableJavascriptFile}"></script>
		<script type="text/javascript" src="/js/jquery.dynatree.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap-datetimepicker.min.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span3">
					<div class="well">
						<div class="row-fluid">
							<#-- add span12 to ensure that input is styled correctly at low and high solutions -->
							<div class="input-append span12" id="observationset-search-container">
								<#-- add span11 to ensure that input is styled correctly at low and high solutions -->
								<input class="span10" id="observationset-search" type="text" placeholder="Search data values">
								<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
						<div class="row-fluid">
							<div class="accordion" id="feature-filters-container">
								<div class="accordion-group">
								    <div class="accordion-heading">
										<a class="accordion-toggle" data-toggle="false" data-parent="#feature-filters-container" href="#feature-filters">Data item filters</a>
									</div>
									<div class="accordion-body collapse in">
										<div class="accordion-inner" id="feature-filters"></div>
									</div>
								</div>
							</div>
						</div>
						<div class="row-fluid">
							<div class="accordion" id="feature-selection-container">
								<div class="accordion-group">
								    <div class="accordion-heading">
										<a class="accordion-toggle" data-toggle="false" data-parent="#feature-selection-container" href="#feature-selection">Data item selection</a>
									</div>
									<div class="accordion-body collapse in">
										<div class="accordion-inner" id="feature-selection"></div>
									</div>
								</div>
							</div>
						</div>			
					</div>		
				</div>
				<div class="span9">
					<div id="dataset-select-container" class="control-group form-horizontal">
						<div id="data-table-header" class="pull-left"></div>
						<div class="controls pull-right">
							<label class="control-label" for="dataset-select">Choose a dataset:</label>
							<select data-placeholder="Choose a Dataset" id="dataset-select">
						<#list dataSets as dataSet>
							<option value="/api/v1/dataset/${dataSet.id?c}"<#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
						</#list>
							</select>
						</div>
					</div>
					<div class="row-fluid data-table-container">
						<table id="data-table" class="table table-striped table-condensed"></table>
					</div>
					<div class="row-fluid data-table-pager-container">
						<a id="download-button" class="btn" href="#">Download as csv</a>
						<div id="data-table-pager" class="pagination pagination-centered"></div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="feature-filter-dialog"></div>
	</body>
</html>
</#if>