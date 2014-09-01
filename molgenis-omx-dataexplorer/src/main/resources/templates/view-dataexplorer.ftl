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
	"jquery.molgenis.table.js",
	"handlebars.min.js"]>

<@header css js/>
    <div id="entity-class" class="well">
			<h3 id="entity-class-name"></h3>
			<span id="entity-class-description"></span>
	</div>
     
    <div class="col-md-4 col-md-offset-8" id="dataset-select-container"<#if hideDatasetSelect??> style="display:none"</#if>>
        <div class="form-horizontal form-group">
            <label class="col-md-4 control-label" for="dataset-select">Choose a dataset:</label>
            <div class="col-md-8">
            	<select class="form-control" id="dataset-select" data-placeholder="Choose a Entity (example: dataset, protocol..." id="dataset-select">
                	<#list entitiesMeta.iterator() as entityMeta>
                    	<option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
                   	</#list>
                </select>
           	</div>
     	</div>
   	</div>
     
	<div class="row">
		<div class="col-md-3">
			<div class="well">
				<div class="row">
                    <div class="form-group">
                        <div class="col-md-12 input-group" <#if hideSearchBox == true> style="display:none"</#if>>
                            <input type="text" class="form-control" id="observationset-search" placeholder="Search data values" autofocus="autofocus"<#if searchTerm??> value="${searchTerm}"</#if>>
                            <span class="input-group-btn">
                                <button id="search-clear-button" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove"></span></button>
                                <button id="search-button" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></span></button>
                            </span>
                        </div>
                    </div>		
				</div>
				<div class="row">
				    <div class="panel">
                        <div class="panel-heading">
                            <h4 class="panel-title"> ${i18n.dataexplorer_data_data_item_filters}</h4>
                        </div>
                        <div class="panel-body">
                            <div class="row" id="feature-filters"></div>
                            <div class="row">
                                <a href="#" id="filter-wizard-btn" class="btn btn-default btn-sm pull-right"><img src="/img/filter-bw.png"> ${i18n.dataexplorer_wizard_button}</a>
                            </div>
                        </div>
                    </div>
				</div>
				<div class="row"<#if hideDataItemSelect == true> style="display:none"</#if>>
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
		<div class="col-md-9">
            <div id="module-nav"></div>
		</div>
	</div>


<script id="filter-wizard-modal-template" type="text/x-handlebars-template">
    <div class="modal" id="filter-wizard-modal" tabindex="-1" role="dialog" aria-labelledby="filter-wizard-modal-label" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title" id="filter-wizard-modal-label">${i18n.dataexplorer_wizard_title}</h4>
                    </div>
                <div class="modal-body">
                    <div class="filter-wizard">
                        <form class="form-horizontal">
                            <ul class="wizard-steps"></ul>
                            <div class="tab-content wizard-page "></div>
                            <ul class="pager wizard">
                                <li class="previous"><a href="#">Previous</a></li><li class="next"><a href="#">Next</a></li>
                                </ul>
                            </form>
                        </div>
                    </div>
                <div class="modal-footer">
                    <a href="#" class="btn btn-default" data-dismiss="modal">${i18n.dataexplorer_wizard_cancel}</a>
                    <a href="#" class="btn btn-primary filter-wizard-apply-btn" data-dismiss="modal">${i18n.dataexplorer_wizard_apply}</a>
                    </div>
                </div>
            </div>
        </div>
</script>
<@footer/>