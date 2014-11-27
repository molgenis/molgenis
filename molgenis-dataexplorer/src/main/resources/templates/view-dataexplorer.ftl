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

<div id="entity-class" class="well top-panel-class">
    <div class="row">
        <div class="col-md-9">
	        <h3 id="entity-class-name"></h3>
	        <span id="entity-class-description"></span>
         </div>
        <div class="col-md-3">

            <div id="dataset-select-container" class="pull-right" <#if hideDatasetSelect??>style="display:none"</#if>>
                <div class="col-md-12">
                    <div class="form-horizontal">
                        <div class="form-group">
                            <div class="row">
                                <div class="col-md-12">
                                    <label class="col-md-12 control-label" for="dataset-select">Choose an entity:</label>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <select class="form-control" id="dataset-select" data-placeholder="Choose an Entity (example: dataset, protocol...">
                                    <#if entitiesMeta?has_content>
                                        <#list entitiesMeta.iterator() as entityMeta>
                                            <option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
                                        </#list>
                                    </#if>
                                    </select>
                                </div>
                            </div>
                        </div>
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
                                	<input type="text" class="form-control" id="observationset-search" placeholder="Search data values" autofocus="autofocus"<#if searchTerm??> value="${searchTerm}"</#if> />
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
                                    <a href="#" id="filter-wizard-btn" class="btn btn-default btn-xs pull-right"><img src=<@resource_href "/img/filter-bw.png"/>> ${i18n.dataexplorer_wizard_button}</a>
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
        <div id="export-button-container" style="display:none">
            <div class="pull-right">
                <!-- Single button -->
               <#if galaxyEnabled?? && galaxyEnabled == true>
                    <div class="btn-group">
                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            Export to... <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-right" role="menu">
                            <li><a id="download-modal-button" class="btn btn-default" data-toggle="modal" data-target="#downloadModal">CSV</a></li>
                            <li><a id="galaxy-export-modal-button" class="btn btn-default" data-toggle="modal" data-target="#galaxy-export-modal">Galaxy</a></li>
                        </ul>
                    </div>
                <#else>
                    <div>
                        <a id="download-modal-button" class="btn btn-default" data-toggle="modal" data-target="#downloadModal">Download as CSV</a>
                    </div>
                </#if>
                </div>
        </div>
        <div id="module-nav"></div>
	</div>
</div>

<#if galaxyEnabled?? && galaxyEnabled == true>
<#-- Galaxy export modal -->
<form name="galaxy-export-form" class="form-horizontal" action="${context_url}/galaxy/export" method="POST">
    <div class="modal" id="galaxy-export-modal" tabindex="-1" role="dialog" aria-labelledby="galaxy-export-modal-label" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title" id="galaxy-export-modal-label">Export data set to Galaxy</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="galaxy-export-url">Galaxy server URL *</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" id="galaxy-export-url" name="galaxyUrl" <#if galaxyUrl??>value="${galaxyUrl?html}" </#if>required><span class="help-block">e.g. https://usegalaxy.org/</span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="galaxy-export-api-key">Galaxy API key *</label>
                        <div class="col-md-5">
                            <input type="password" class="form-control" id="galaxy-export-api-key" name="galaxyApiKey" <#if galaxyApiKey??>value="${galaxyApiKey?html}" </#if>required><span class="help-block">See 'Select API Keys' in menu 'User' on e.g. https://usegalaxy.org/</span>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Export to Galaxy</button>
                </div>
            </div>
        </div>
    </div>
</form>
</#if>

<#-- CSV download modal -->
<div class="modal" id="downloadModal" tabindex="-1" role="dialog" aria-labelledby="download-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="download-modal-label">Download as csv</h4>
            </div>

            <div class="modal-body">
                <div class="form-group form-horizontal">
                    <div class="row">
                        <div class="control-group">
                            <label class="col-md-3 control-label">As column names I want:</label>
                            <div class="controls col-md-9">
                                <label class="radio">
                                    <input type="radio" name="colNames" value="ATTRIBUTE_LABELS" checked> Attribute Labels
                                </label>
                                <label class="radio">
                                    <input type="radio" name="colNames" value="ATTRIBUTE_NAMES">Attribute Names
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button id="download-button" class="btn btn-primary">Download</button>
            </div>

        </div>
    </div>
</div>


<script id="filter-wizard-modal-template" type="text/x-handlebars-template">   
    <div class="modal" id="filter-wizard-modal" tabindex="-1" role="dialog" aria-labelledby="filter-wizard-modal-label" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                
                <div class="modal-header">
                    <h4 class="modal-title" id="filter-wizard-modal-label">${i18n.dataexplorer_wizard_title}</h4>
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
                	<a href="#" class="btn btn-default" data-dismiss="modal">${i18n.dataexplorer_wizard_cancel}</a>
                    <a href="#" class="btn btn-primary filter-wizard-apply-btn" data-dismiss="modal">${i18n.dataexplorer_wizard_apply}</a>
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
					<a href="#" class="btn btn-default" data-dismiss="modal">${i18n.dataexplorer_wizard_cancel}</a>
					<a href="#" class="btn btn-primary filter-apply-btn" data-dismiss="modal">${i18n.dataexplorer_wizard_apply}</a>
				</div>	
			</div>
		</div>
	</div>
</script>
<@footer/>