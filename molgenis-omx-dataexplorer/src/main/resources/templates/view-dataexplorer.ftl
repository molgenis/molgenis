<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
	"jquery-ui-1.9.2.custom.min.css",
	"chosen.css",
	"jquery.bootstrap.wizard.css",
	"bootstrap-datetimepicker.min.css",
	"ui.fancytree.min.css",
	"jquery.molgenis.table.css",
	"dataexplorer.css",
	"select2.css"]>
<#assign js=[
	"dalliance-compiled.js",
	"jquery-ui-1.9.2.custom.min.js",
	"chosen.jquery.min.js",
	"dataexplorer.js",
	"jquery.bootstrap.pager.js",
	"jquery.bootstrap.wizard.min.js",
	"bootstrap-datetimepicker.min.js",
	"dataexplorer-filter.js",
	"dataexplorer-wizard.js",
	"dataexplorer-charts.js",
	"dataexplorer-aggregates.js",
	"highstock-1.3.6/highstock.js",
	"highstock-1.3.6/highcharts-more.js",
	"jquery.fancytree.min.js",
	"jquery.molgenis.tree.js",
	"jquery.molgenis.table.js",
	"select2.min.js",
	"jquery.molgenis.xrefsearch.js"]>

<@header css js/>
    <script>
    <#--instanciate the Dalliance browser with settings from the controller-->
    var genomeBrowserDataSets = {};
    <#if genomeBrowserSets??>
        <#list genomeBrowserSets?keys as prop>
            genomeBrowserDataSets['${prop}'] = '${genomeBrowserSets[prop]}';
        </#list>
        var dalliance = new Browser({
            ${initLocation},
            coordSystem: ${coordSystem},
            chains: ${chains},
            sources: ${sources},
            browserLinks: ${browserLinks},
            searchEndpoint: ${searchEndpoint},
            karyotypeEndpoint: ${karyotypeEndpoint}
        });
        $(document).on('show', '#genomebrowser .collapse', function() {
            $(this).parent().find(".icon-chevron-right").removeClass("icon-chevron-right").addClass("icon-chevron-down");
        }).on('hide', '#genomebrowser .collapse', function() {
                    $(this).parent().find(".icon-chevron-down").removeClass("icon-chevron-down").addClass("icon-chevron-right");
                });
    </#if>
    	var showWizard = ${wizard?string('true', 'false')};
    </script>
	<div class="row-fluid">
		<div class="row-fluid pull-right form-horizontal">
			<div id="dataset-select-container" class="pull-right form-horizontal">
				<label class="control-label" for="dataset-select">Choose a dataset:</label>
				<div class="controls">
					<select data-placeholder="Choose a Entity (example: dataset, protocol..." id="dataset-select">
						<#list entitiesMeta.iterator() as entityMeta>
							<option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
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
								<div class="accordion-inner">
									<div class="row-fluid" id="feature-selection"></div>
									<div class="row-fluid" id="data-options">
										<a href="#" id="filter-wizard-btn" class="btn btn-small pull-right"><img src="/img/filter-bw.png"> Wizard</a>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>			
			</div>		
		</div>
		<div class="span9">			
			<#--Charts-->
			<#if mod_charts>
				<#include "/charts-forms/view-scatterplot.ftl" parse=false>
				<#include "/charts-forms/view-boxplot.ftl" parse=false>
				<#include "/charts-forms/view-heatmap.ftl" parse=false>
			</#if>
				
			<div class="tabbable">
				<ul id="tabs" class="nav nav-tabs">
				<#if mod_data>
					<li id="tab-data"><a href="#dataset-data-container" data-toggle="tab"><img src="/img/grid-icon.png"> Data</a></li>
				</#if>
				<#if mod_aggregates>
					<li id="tab-aggregates"><a href="#dataset-aggregate-container" data-toggle="tab"><img src="/img/aggregate-icon.png"> Aggregates</a></li>
				</#if>
				<#if mod_charts>
					<li id="tab-charts"><a href="#chart-container" data-toggle="tab"><img src="/img/chart-icon.png" alt="charts"> Charts</a></li>
				</#if>
				</ul>
				<div class="tab-content">
				<#if mod_data>
					<div class="tab-pane active" id="dataset-data-container">
						<div id="dataexplorer-grid-data">
                            <div class="accordion" id="genomebrowser">
                                <div class="accordion-group">
                                    <div class="accordion-heading">
                                        <a class="accordion-toggle" data-toggle="collapse" href="#dalliance"><i class="icon-chevron-down"></i> Genome Browser</a>
                                    </div>
                                    <div id="dalliance" class="accordion-body collapse in">
                                        <div class="accordion-inner">
                                            <div id="svgHolder"></div>
                                            <div class="pull-right"><a id="genomebrowser-filter-button" class="btn btn-small"><img src="/img/filter-bw.png"> apply filter</a></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row-fluid data-table-container" id="data-table-container"></div>
							<div class="row-fluid data-table-pager-container">
								<a id="download-button" class="btn" href="#">Download as csv</a>
							</div>
						</div>
					</div>
				</#if>
				<#if mod_aggregates>
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
				</#if>
				<#if mod_charts>
					<div class="tab-pane" id="chart-container">
						<div class="row-fluid">
							<div class="btn-group" class="span9">
								<a href="#chart-designer-modal-scatterplot" id="chart-designer-modal-scatterplot-button" role="button" class="btn" data-toggle="modal">Create scatter plot <i class="icon-plus"></i></a>
								<a href="#chart-designer-modal-boxplot" id="chart-designer-modal-boxplot-button" role="button" class="btn" data-toggle="modal">Create box plot <i class="icon-plus"></i></a>
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
	</div>
<@footer/>