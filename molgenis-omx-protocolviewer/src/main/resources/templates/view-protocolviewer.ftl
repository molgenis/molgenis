<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["ui.fancytree.min.css", "chosen.css", "protocolviewer.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "chosen.jquery.min.js", "protocolviewer.js", "jquery.fancytree.min.js", "jquery.catalog.js", "jquery.bootstrap.pager.js", "jquery.validate.min.js"]>
<@header css js/>
<#if authenticated>	
	<script>
		molgenis.Catalog.setEnableSelection(true);
	</script>
</#if>
	<div class="row-fluid">			
    <#if (catalogs?size == 0)>
        <span>No active catalogs</span>
    <#else>
        <div class="row-fluid grid<#if (catalogs?size == 1)> hide</#if>">
            <div id="catalog-select-container" class="control-group form-horizontal pull-right">
                <label class="control-label" for="catalog-select">Choose a catalog:</label>
                <div class="controls">
                    <select data-placeholder="Choose a catalog" id="catalog-select">
                <#list catalogs as catalog>
                        <option value="${catalog.id}"<#if catalog_index == 0> selected</#if>>${catalog.name}</option>
                </#list>
                    </select>
                </div>
            </div>
        </div>
        <div class="row-fluid grid">
            <div class="span4">
                <p class="box-title">Browse variables</p>
                <div id="catalog-container"></div>
            </div>
            <div class="span8">
                <div class="row-fluid grid" id="feature-information">
                    <p class="box-title">Variable description</p>
                    <div id="feature-details">
                    </div>
                </div>
			<#if authenticated>               
                <div class="row-fluid grid" id="feature-shopping">
                    <p class="box-title">Variable selection</p>
                    <div id="feature-selection-table-container">
                    </div>
	                <div id="feature-selection-table-pager">
	                </div>    
                </div>
                <div class="row-fluid grid" id="feature-shopping-controls">
                    <div class="span9">
                        <div class="btn-group pull-left">
                    <#if enableDownloadAction>
                            <button class="btn" id="download-xls-button">Download</button>
                    </#if>
                        </div>
                    </div>
                    <div class="span3">
                    <div id="orderdata-modal-container"></div>
                    <div id="ordersview-modal-container"></div>
                    <#if enableOrderAction>
                        <div class="btn-group pull-right">
                            <a class="modal-href btn" href="/plugin/protocolviewer/orders/view" data-target="ordersview-modal-container" id="ordersview-href-btn">View Submissions</a>
                            <a class="modal-href btn btn-primary" href="/plugin/protocolviewer/order" data-target="orderdata-modal-container" id="orderdata-href-btn">Submit</a>
                        </div>
                    </#if>
                    </div>
                </div>
			</#if>
            </div>
        </div>
    </#if>
	</div>
<@footer/>