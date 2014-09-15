<#include "resource-macros.ftl">
<div class="row">
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-12">        
                <div id="feature-select-container">
                    <div class="form-horizontal">
                        <div class="form-group">
                        	<label class="col-md-2 control-label" for="feature-select">${i18n.dataexplorer_aggregates_group_by}</label>
                        	<div class="col-md-4">
                            	<div id="feature-select"></div>
                        	</div>
                    	</div>                	
                    	<div class="form-group">
                            <label class="col-md-2 control-label"for="distinct-attr-select">${i18n.dataexplorer_aggregates_distinct}</label>
                            <div class="col-md-3">
                                <div id="distinct-attr-select"></div>
                            </div>
                    	</div>
                	</div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="data-table-container form-horizontal" id="dataexplorer-aggregate-data">
                	<div id="aggregate-table-container"></div>
                </div>
            </div>
        </div>                
    </div>
</div>
<script>
	$.when($.ajax("<@resource_href "/js/dataexplorer-aggregates.js"/>", {'cache': true}))
		.then(function() {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
</script>
<script id="aggregates-no-result-message-template" type="text/x-handlebars-template">
    <br><div>${i18n.dataexplorer_aggregates_no_result_message}<div>
</script>

