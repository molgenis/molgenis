<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
	"jquery.bootstrap.wizard.css",
	"bootstrap-datetimepicker.min.css",
	"ui.fancytree.min.css",
	"jquery-ui-1.9.2.custom.min.css",
	"select2.css",
	"iThing-min.css",
	"bootstrap-switch.min.css",
	"dataexplorer.css",
	"dataexplorer-filter.css",
	"diseasematcher.css"]>
<#assign js=[
	"jquery-ui-1.9.2.custom.min.js",
	"jquery.bootstrap.wizard.min.js",
	"bootstrap-datetimepicker.min.js",
	"dataexplorer-filter.js",
	"dataexplorer-filter-dialog.js",
	"dataexplorer-filter-wizard.js",
	"jquery.fancytree.min.js",
	"jquery.molgenis.tree.js",
	"select2.min.js",
	"jQEditRangeSlider-min.js",
	"bootstrap-switch.min.js",
	"jquery.molgenis.xrefmrefsearch.js",
	"dataexplorer.js",
	"jquery.molgenis.table.js",
	"handlebars.min.js"]>

<@header css js/>
    <script>
        molgenis.dataexplorer.filter.wizard.setWizardTitle('${wizardtitle}');
   	</script>
    <div id="entity-class" class="well">
		<div class="row">
			<h3 id="entity-class-name"></h3>
			<col-md- id="entity-class-description"></col-md->
		</div>
	</div>
     
    <div class="pull-right"<#if hideDatasetSelect??> style="display:none"</#if>>
   		<div class="row  form-horizontal">
       		<div id="dataset-select-container" class="pull-right form-horizontal">
            	<label class="col-md-3 control-label" for="dataset-select">Choose a dataset:</label>
                <div class="col-md-9">
                	<select data-placeholder="Choose a Entity (example: dataset, protocol..." id="dataset-select">
                    	<#list entitiesMeta.iterator() as entityMeta>
                        	<option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
                       	</#list>
                    </select>
               	</div>
         	</div>
     	</div>
   	</div>
     
	<div class="row">
		<div class="col-md-3">
			<div class="well">
				<div class="row">
					<#-- add col-md-12 to ensure that input is styled correctly at low and high solutions -->
					<div class="group-append col-md-12" id="observationset-search-container">
						<#-- add col-md-10 to ensure that input is styled correctly at low and high solutions -->
						<input class="col-md-10" id="observationset-search" type="text" <#if searchTerm??>value="${searchTerm}"</#if>  placeholder="Search data values"></input>
						<button class="btn btn-default" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
					</div>					
				</div>
				<div class="row">
					<div class="accordion" id="feature-filters-container">
						<div class="accordion-group">
						    <div class="accordion-heading">
								<a class="accordion-toggle" data-toggle="false" data-parent="#feature-filters-container" href="#feature-filters">Data item filters</a>
							</div>
							<div class="accordion-body collapse in">
								<div class="accordion-inner">
								    <div class="row" id="feature-filters"></div>
								    <div class="row">
								    	<a href="#" id="filter-wizard-btn" class="btn btn-small pull-right"><img src="/img/filter-bw.png"> ${wizardbuttontitle}</a>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
                <div class="row"<#if hideDataItemSelect??> style="display:none"</#if>>
                    <div class="accordion" id="feature-selection-container">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="false" data-parent="#feature-selection-container" href="#feature-selection">Data item selection</a>
                            </div>
                            <div class="accordion-body collapse in">
                                <div class="accordion-inner">
                                    <div class="row" id="feature-selection"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
			</div>		
		</div>
		
		<div id="module-nav"></div>
	
	</div>
<@footer/>