<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
	"chosen.css",
	"jquery.bootstrap.wizard.css",
	"bootstrap-datetimepicker.min.css",
	"ui.fancytree.min.css",
	"jquery-ui-1.9.2.custom.min.css",
	"select2.css",
	"dataexplorer.css"]>
<#assign js=[
	"jquery-ui-1.9.2.custom.min.js",
	"chosen.jquery.min.js",
	"jquery.bootstrap.wizard.min.js",
	"bootstrap-datetimepicker.min.js",
	"dataexplorer-filter.js",
	"dataexplorer-wizard.js",
	"jquery.fancytree.min.js",
	"jquery.molgenis.tree.js",
	"select2.min.js",
	"jquery.molgenis.xrefsearch.js",
	"dataexplorer.js"]>
<@header css js/>
    <script>
    	molgenis.dataexplorer.setShowWizardOnInit(${wizard?string('true', 'false')});
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
						<#-- add span10 to ensure that input is styled correctly at low and high solutions -->
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
		<div class="span9" id="module-nav"></div>
	</div>
<@footer/>