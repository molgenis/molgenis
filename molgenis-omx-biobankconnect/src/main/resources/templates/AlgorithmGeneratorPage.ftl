<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="row-fluid pull-right form-horizontal">
			<div id="dataset-select-container" class="pull-right form-horizontal">
				<label class="control-label" for="dataset-select">Choose a dataset:</label>
				<div class="controls">
					<select data-placeholder="Choose a Dataset" id="dataset-select">
						<#assign dataSet=wizard.derivedDataSet>
						<option value="/api/v1/dataset/${dataSet.id?c}">${dataSet.name}</option>
					</select>
				</div>
			</div>
		</div>	
		<div id="genomebrowser"></div>
	</div>
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
								<div id="data-options"> 
									<a href="#filter-dialog-modal" id="wizard-button" class="btn btn-small" data-toggle="modal"><img src="/img/filter-bw.png">wizard</a>
								</div>
							</div>
						</div>
					</div>
				</div>			
			</div>		
		</div>
		<div class="span9">
			<div id="filter-dialog-modal-container">
				<div id="filter-dialog-modal" class="modal hide" tabindex="-1" role="dialog">
				</div>
			</div>
			
			<!--Charts-->
			<#if app_dataexplorer_include_charts?has_content && app_dataexplorer_include_charts>
				<#include "/charts-forms/view-scatterplot.ftl" parse=false>
				<#include "/charts-forms/view-boxplot.ftl" parse=false>
				<#include "/charts-forms/view-heatmap.ftl" parse=false>
			</#if>
				
			<div class="tabbable">
				<ul id="tabs" class="nav nav-tabs">
					<li class="active"><a href="#dataset-data-container" data-toggle="tab"><img src="/img/grid-icon.png"> Data</a></li>  
					<li><a href="#dataset-aggregate-container" data-toggle="tab"><img src="/img/aggregate-icon.png"> Aggregates</a></li>
					<!--Charts-->
					<#if app_dataexplorer_include_charts?has_content && app_dataexplorer_include_charts>
						<li><a href="#chart-container" data-toggle="tab"><img src="/img/chart-icon.png" alt="charts"> Charts</a></li>
					</#if>
				</ul>
				<div class="tab-content">
					<div class="tab-pane active" id="dataset-data-container">
						<div id="dataexplorer-grid-data">
                            <div class="row-fluid data-table-container" >
								<table id="data-table" class="table table-striped table-condensed"></table>	
							</div>
							<div class="row-fluid data-table-pager-container">
									
								<div id="nrOfDataItems" class="pull-left"></div>
								<a id="download-button" class="btn" href="#">Download as csv</a>
								<div id="data-table-pager" class="pagination pagination-centered"></div>
							</div>
						</div>
					</div>
					<div class="tab-pane" id="dataset-aggregate-container">
						<div id="feature-select-container">
							<label class="control-label" for="feature-select">Aggregate by:</label>
							<div id="feature-select" class="controls">
							</div>
						</div>
						<div class="row-fluid data-table-container form-horizontal" id="dataexplorer-aggregate-data">
							<div id="aggregate-table-container"></div>
						</div>
					</div>
					<!--Charts-->
					<#if app_dataexplorer_include_charts?has_content && app_dataexplorer_include_charts>
						<div class="tab-pane" id="chart-container">
							<div class="row-fluid">		
								<div class="btn-group" class="span9">
									<a href="#chart-designer-modal-scatterplot" id="chart-designer-modal-scatterplot-button" role="button" class="btn" data-toggle="modal">Create scatter plot <i class="icon-plus"></i></a>
									<a href="#chart-designer-modal-boxplot" id="chart-designer-modal-boxplot-button" role="button" class="btn" data-toggle="modal">Create box plot <i class="icon-plus"></i></a>
									<!-- TODO Heat map
										<a href="#chart-designer-modal-heatmap" id="chart-designer-modal-heatmap-button" role="button" class="btn" data-toggle="modal">Heat map <i class="icon-plus"></i></a>
									-->							
								</div>
							</div>
							<div class="row-fluid">
								<div id="chart-view" class="span9"></div>
							</div>
						</div>
					</#if>
				</div>
			</div>
		</div>
		<div class="feature-filter-dialog"></div>	
	</div>
	<script src="/js/jquery.bootstrap.pager.js" type="text/javascript"></script>
	<script src="/js/jquery.dynatree.min.js" type="text/javascript"></script>
	<script src="/js/dataexplorer.js" type="text/javascript"></script>
	<script src="/js/SingleObservationSetTable.js" type="text/javascript"></script>
	<script src="/js/MultiObservationSetTable.js" type="text/javascript"></script>
	<script src="/js/chosen.jquery.min.js" type="text/javascript"></script>
	<link rel="stylesheet" href="/css/chosen.css" type="text/css">
	<link rel="stylesheet" href="/css/ui.dynatree.css" type="text/css">
	<link rel="stylesheet" href="/css/dataexplorer.css" type="text/css">
	<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
	<script type="text/javascript">
		var genomeBrowserDataSets = {};
		molgenis.setContextUrl('/menu/main/dataexplorer');
	</script>
</form>