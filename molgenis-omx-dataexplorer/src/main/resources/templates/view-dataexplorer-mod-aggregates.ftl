<#include "resource-macros.ftl">
<div class="row">
    <div class="col-md-12">
        <div id="feature-select-container">
        	<label class="col-md-3 control-label" for="feature-select">Group by:</label>
        	<div id="feature-select" class="controls">
        	</div>
        </div>
        <div class="row data-table-container form-horizontal" id="dataexplorer-aggregate-data">
        	<div id="aggregate-table-container"></div>
        </div>
    </div>
</div>
<script>
	$.when($.ajax("<@resource_href "/js/dataexplorer-aggregates.js"/>", {'cache': true}))
		.then(function() {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
</script>
