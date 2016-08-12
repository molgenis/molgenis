<#include "resource-macros.ftl">

<div class="row" id="aggregatesContainer">
    <div id="feature-select-container">
        <div class="row">
            <div id="feature-select" class="col-md-12">
                <label class="col-md-1 control-label" for="feature-select"
                       style="white-space: nowrap;">${i18n.dataexplorer_aggregates_group_by?html}</label>
                <div class="col-md-3" id="x-aggr-div"></div>
                <div class="pull-left" class="col-md-1">
                    <p class="form-control-static">x</p>
                </div>
                <div class="col-md-3" id="y-aggr-div"></div>
            </div>
        </div>
        <div class="row">
            <div id="distinct-attr" class="col-md-12">
                <label class="col-md-1 control-label"
                       for="distinct-attr-select">${i18n.dataexplorer_aggregates_distinct?html}</label>
                <div class="col-md-3" id="distinct-attr-select"></div>
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
<script>
    $.when($.ajax("<@resource_href "/js/dataexplorer-aggregates.js"/>", {'cache': true}))
            .then(function () {
                molgenis.dataexplorer.aggregates.createAggregatesTable();
            });
</script>