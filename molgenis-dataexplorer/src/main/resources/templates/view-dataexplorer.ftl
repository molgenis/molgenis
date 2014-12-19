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
	"moment-with-locales.min.js",
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
	"jquery.molgenis.table.js"]>

<@header css js/>
<div class="row">
    <div class="col-md-12">
        <div id="entity-class" class="well well-sm">
            <div class="row">
                <div class="col-md-9">
                    <h3 id="entity-class-name"></h3>
                    <span id="entity-class-description"></span>
                </div>
                <div class="col-md-3">
                    <div id="dataset-select-container" class="pull-right" <#if hideDatasetSelect??>style="display:none"</#if>>
                        <select class="form-control" id="dataset-select" data-placeholder="Choose an Entity">
                                <option value=""></option><#-- Required for placeholder to work with select2 -->
                        <#if entitiesMeta?has_content>
                            <#list entitiesMeta.iterator() as entityMeta>
                                <option value="/api/v1/${entityMeta.name?html}"<#if selectedEntityName?? && (entityMeta.name == selectedEntityName)> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label?html}<#else>${entityMeta.name?html}</#if></option>
                            </#list>
                        </#if>
                        </select>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
	<div class="col-md-3">
		<div class="well well-sm">
			<div class="row">
                <div class="col-md-12">
                	<div class="form-horizontal">
                    	<div class="form-group">
                        	<div class="col-md-12">
                            	<div class="input-group" <#if hideSearchBox == true> style="display:none"</#if>>
                                	<input type="text" class="form-control" id="observationset-search" placeholder="Search data values" autofocus="autofocus"<#if searchTerm??> value="${searchTerm?html}"</#if> />
                                	<span class="input-group-btn">
                                    	<button id="search-clear-button" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove"></span></button>
                                    	<button id="search-button" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></span></button>
                                	</span>
                            	</div>
                        	</div>   
                    	</div>
                    </div>
                </div>
			</div>
			<div class="row">
                <div class="col-md-12">
    			    <div class="panel">
                        <div class="panel-heading">
                            <h4 class="panel-title"> ${i18n.dataexplorer_data_data_item_filters}</h4>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <div id="feature-filters"></div>    
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <a href="#" id="filter-wizard-btn" class="btn btn-default btn-xs pull-right"><img src=<@resource_href "/img/filter-bw.png"/>> ${i18n.dataexplorer_wizard_button?html}</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
			</div>
			<div class="row"<#if hideDataItemSelect == true> style="display:none"</#if>>
                <div class="col-md-12">
                    <div class="panel">
                        <div class="panel-heading">
                            <h4 class="panel-title">Data item selection</h4>
                        </div>
                        <div class="panel-body">
                            <div id="feature-selection"></div>
                        </div>
                    </div>
                </div>
            </div>
		</div>		
	</div>
	<div class="col-md-9">
		<div id="module-nav"></div>
	</div>
</div>

<script id="filter-wizard-modal-template" type="text/x-handlebars-template">
    <div class="modal" id="filter-wizard-modal" tabindex="-1" role="dialog" aria-labelledby="filter-wizard-modal-label" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                
                <div class="modal-header">
                    <h4 class="modal-title" id="filter-wizard-modal-label">${i18n.dataexplorer_wizard_title?html}</h4>
                    <button type="button" class="close" data-dismiss="modal">
                    	<span aria-hidden="true">&times;</span>
                    	<span class="sr-only">Close</span>
                	</button>
                </div>
                
                <div class="modal-body">
                    <div class="filter-wizard">
                        <form class="form-horizontal">
                            <ul class="wizard-steps"></ul>
                            <div class="tab-content wizard-page"></div>
                            <ul class="pager wizard">
                            	<li class="previous"><a href="#">Previous</a></li>
                            	<li class="next"><a href="#">Next</a></li>
                            </ul>
                        </form>
                    </div>
                </div>
                
                <div class="modal-footer">
                	<a href="#" class="btn btn-default" data-dismiss="modal">${i18n.dataexplorer_wizard_cancel?html}</a>
                    <a href="#" class="btn btn-primary filter-wizard-apply-btn" data-dismiss="modal">${i18n.dataexplorer_wizard_apply?html}</a>
                </div>
                
            </div>
    	</div>
    </div>
</script>

<script id="filter-modal-template" type="text/x-handlebars-template">
	<div id="filter-modal" class="modal" tabindex="-1" aria-labelledby="filter-modal-label" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
					<h4 class="modal-title filter-title" id="filter-modal-label"></h4>
				</div>
				
				<div class="modal-body">
					<legend>Description</legend>
					<p class="filter-description"></p>
					<legend>Filter</legend>
					<form class="form-horizontal"></form>
				</div>
				
				<div class="modal-footer">
					<a href="#" class="btn btn-default" data-dismiss="modal">${i18n.dataexplorer_wizard_cancel?html}</a>
					<a href="#" class="btn btn-primary filter-apply-btn" data-dismiss="modal">${i18n.dataexplorer_wizard_apply?html}</a>
				</div>	
			</div>
		</div>
	</div>
</script>
<@footer/>