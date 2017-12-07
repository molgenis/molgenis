<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
"jquery.molgenis.tree.css",
"ui.fancytree.min.css",
"dataexplorer.css",
"dataexplorer-filter.css"]>
<#assign js=[
"jquery.bootstrap.wizard.min.js",
"dataexplorer-filter.js",
"dataexplorer-filter-dialog.js",
"dataexplorer-filter-wizard.js",
"jquery.fancytree.min.js",
"jquery.molgenis.tree.js",
"jquery.molgenis.xrefmrefsearch.js",
"dataexplorer.js",
"dataexplorer-filter-rsql.js",
"bootbox.min.js"]>

<@header css js/>

<script>
    window.hasTrackingId = ${hasTrackingId?c}
    window.hasMolgenisTrackingId = ${hasMolgenisTrackingId?c}
</script>

<div class="row">
    <div class="col-md-12">
        <div id="entity-class" class="well well-sm">
            <div class="row">
                <div class="col-md-7">
                    <h3 id="entity-class-name"></h3>
                <#if showNavigatorLink??>
                    <span
                            id="entity-package-path" <#if !showNavigatorLink?? || showNavigatorLink == false>
                            style="display:none"</#if>></span>
                </#if>
                    <span id="entity-class-description"></span>
                </div>
                <div class="col-md-4">
                    <div id="dataset-select-container" class="pull-right">
                        <select class="form-control" id="dataset-select" data-placeholder="Choose an Entity">
                            <option value=""></option><#-- Required for placeholder to work with select2 -->
                        <#if entitiesMeta?has_content>
                            <#list entitiesMeta?keys as fullyQualifiedName>
                                <option value="${fullyQualifiedName?html}"<#if selectedEntityName?? && (fullyQualifiedName == selectedEntityName)>
                                        selected</#if>>${entitiesMeta[fullyQualifiedName].label?html}</option>
                            </#list>
                        </#if>
                        </select>
                    </div>
                    <button id="copy-data-btn" type="button" class="btn btn-default pull-right hidden">
                        <span class="glyphicon glyphicon-duplicate" aria-hidden="true"></span>
                    </button>
                </div>
            <#if isAdmin?has_content && isAdmin>
                <div class="col-md-1">
                    <div class="dropdown">
                        <button class="btn btn-danger dropdown-toggle" type="button" id="dropdownMenu1"
                                data-toggle="dropdown" aria-expanded="true">
                            Delete <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="dropdownMenu1">
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="#"
                                                       id="delete-data-btn">Data</a></li>
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="#"
                                                       id="delete-data-metadata-btn">Data and meta data</a></li>
                        </ul>
                    </div>
                </div>
            </#if>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-md-3" id="selectors">
        <div class="well well-sm">
            <div class="row">
                <div class="col-md-12">
                    <div class="form-horizontal">
                        <div class="form-group">
                            <div class="col-md-12">
                                <div class="input-group" <#if plugin_settings.get("searchbox") == false>
                                     style="display:none"</#if>>
                                    <input type="text" class="form-control" id="observationset-search"
                                           placeholder="Search data values" autofocus="autofocus"/>
                                    <span class="input-group-btn">
                                    	<button id="search-clear-button" class="btn btn-default" type="button"><span
                                                class="glyphicon glyphicon-remove"></span></button>
                                    	<button id="search-button" class="btn btn-default" type="button"><span
                                                class="glyphicon glyphicon-search"></span></button>
                                	</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="panel panel-primary">
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
                                    <a href="#" id="filter-wizard-btn" class="btn btn-default btn-xs pull-right"><img
                                            src="<@resource_href "/img/filter-bw.png"/>"> ${i18n.dataexplorer_wizard_button?html}
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row"<#if plugin_settings.item_select_panel == false> style="display:none"</#if>>
                <div class="col-md-12">
                    <div class="panel panel-primary">
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
    <div class="col-md-9" id="modules">
        <div id="module-nav"></div>
    </div>
</div>

<script id="filter-wizard-modal-template" type="text/x-handlebars-template">
    <div class="modal" id="filter-wizard-modal" tabindex="-1" role="dialog" aria-labelledby="filter-wizard-modal-label"
         aria-hidden="true">
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
                    <a href="#" class="btn btn-primary filter-wizard-apply-btn"
                       data-dismiss="modal">${i18n.dataexplorer_wizard_apply?html}</a>
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
                    <button type="button" class="close" data-dismiss="modal"><span
                            aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
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
                    <a href="#" class="btn btn-primary filter-apply-btn"
                       data-dismiss="modal">${i18n.dataexplorer_wizard_apply?html}</a>
                </div>
            </div>
        </div>
    </div>
</script>

<div id="negotiator-modal" class="modal" tabindex="-1" aria-labelledby="negotiator-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span
                        aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="negotiator-modal-label">${i18n.dataexplorer_directory_export_dialog_label?html}</h4>
            </div>

            <div class="modal-body">
                <h5 id="negotiator-message"></h5>

                <div class="panel panel-default">
                    <div class="panel-heading">${i18n.dataexplorer_directory_export_dialog_enabled_collections_header?html}</div>
                    <div class="panel-body" style="max-height: 100px; overflow-y: auto">
                        <ul id="enabled-collections-list"></ul>
                    </div>
                </div>

                <div class="panel panel-default">
                    <div class="panel-heading">${i18n.dataexplorer_directory_export_dialog_disabled_collections_header?html}</div>
                    <div class="panel-body" style="max-height: 100px; overflow-y: auto">
                        <ul id="disabled-collections-list"></ul>
                    </div>
                </div>

            </div>

            <div class="modal-footer">
                <a href="#" class="btn btn-default" data-dismiss="modal">${i18n.dataexplorer_directory_export_dialog_no?html}</a>
                <a href="#" id="negotiator-apply-btn" class="btn btn-primary"
                   data-dismiss="modal">${i18n.dataexplorer_directory_export_dialog_yes?html}</a>
            </div>
        </div>
    </div>
</div>
<@footer/>