<#include "resource-macros.ftl">
<div class="row">
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-12">        
                <div id="feature-select-container">
                	<label class="col-md-3 control-label" for="feature-select">${i18n.dataexplorer_aggregates_group_by}</label>
                	<div id="feature-select" class="controls">
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

