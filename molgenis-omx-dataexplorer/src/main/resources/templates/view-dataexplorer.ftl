<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "chosen.css", "bootstrap-datetimepicker.min.css", "dataexplorer.css", "ui.dynatree.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "chosen.jquery.min.js", "dataexplorer.js", "${resultsTableJavascriptFile}", "jquery.bootstrap.pager.js", "jquery.bootstrap.wizard.min.js", "jquery.dynatree.min.js", "bootstrap-datetimepicker.min.js"]>
<@header css js/>
	<#if entityExplorerUrl??>
		<script>top.molgenis.setEntityExplorerUrl('${entityExplorerUrl}');</script>
	</#if>
	<div class="row-fluid">
		<div class="row-fluid pull-right form-horizontal">
			<div id="dataset-select-container" class="pull-right form-horizontal">
				<label class="control-label" for="dataset-select">Choose a dataset:</label>
				<div class="controls">
					<select data-placeholder="Choose a Dataset" id="dataset-select">
						<#list dataSets as dataSet>
							<option value="/api/v1/dataset/${dataSet.id?c}"<#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
						</#list>
					</select>
				</div>
			</div>
		</div>	
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
					<div class="input-append span12">
						<a href="#filter-dialog-modal" id="filter-wizard-button" role="button" class="btn btn-primary" data-toggle="modal">start filter wizard</a>
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
<<<<<<< HEAD
			<div class="pull-right">
				<label class="control-label" for="dataset-select">View:</label>
				<div id="switchview" class="pull-right">
					<div id="dataDiv" class="<#if !authenticated>view-disabled<#else>view-enabled</#if>">
						<span class="viewer" id="data">data<img id="dataViewIcon" src="/img/grid-icon.png"></img>
						</span>
					</div>
					<div id="aggregateDiv">
						<span class="viewer" id="aggregate">aggregate<img id="aggregateViewIcon" src="/img/aggregate-icon.png"></img></span>
					</div>
				</div>
			</div>
			<div class="controls pull-left">
				<label class="control-label" for="dataset-select">Choose a dataset:</label>
				<select data-placeholder="Choose a Dataset" id="dataset-select">
					<#list dataSets as dataSet>
						<option value="/api/v1/dataset/${dataSet.id?c}"<#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
					</#list>
				</select>
			</div>
		</div>
		<div id="filter-dialog-modal-container">
			<div id="filter-dialog-modal" class="modal hide" tabindex="-1" role="dialog">
			</div>
		</div>
		<div class="span9">
			<legend></legend>
			<div id="dataexplorer-grid-data">	
				<div class="row-fluid data-table-container" >	
					<table id="data-table" class="table table-striped table-condensed"></table>	
				</div>	
				<div class="row-fluid data-table-pager-container">
					<div id="nrOfDataItems" class="pull-left"></div>
					<a id="download-button" class="btn" href="#">Download as csv</a>
					<div id="data-table-pager" class="pagination pagination-centered"></div>			
=======
				<div class="tabbable">
					<ul class="nav nav-tabs">
						<li class="active"><a href="#dataset-data-container" data-toggle="tab"><img src="/img/grid-icon.png"> Data</a></li>  
						<li><a href="#dataset-aggregate-container" data-toggle="tab"><img src="/img/aggregate-icon.png"> Aggregates</a></li>
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
							<div class="row-fluid data-table-container form-horizontal" id="dataexplorer-aggregate-data">
								<div id=""feature-select-container">
									<label class="control-label" for="feature-select">Aggregate by:</label>
									<div id="feature-select" class="controls">
									</div>
								</div>
								<div id="aggregate-table-container"></div>
							</div>
						</div>
					</div>
>>>>>>> a9082e1cdff9655ab77e2c2170fb5cf7d2e4017a
				</div>
			</div>
		</div>
		<div class="feature-filter-dialog"></div>	
	</div>
<@footer/>