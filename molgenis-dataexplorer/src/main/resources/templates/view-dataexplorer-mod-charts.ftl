<#include "resource-macros.ftl">
<#include "/charts-forms/view-scatterplot.ftl" parse=false>
<#include "/charts-forms/view-boxplot.ftl" parse=false>
<#include "/charts-forms/view-heatmap.ftl" parse=false>
<div class="row">
    <div class="col-md-12">
        <div class="btn-group" id="chartCreationButtons">
            <a href="#chart-designer-modal-scatterplot" id="chart-designer-modal-scatterplot-button" role="button"
               class="btn btn-default" data-toggle="modal">Create scatter plot <span
                    class="glyphicon glyphicon-plus"></span></a>
            <a href="#chart-designer-modal-boxplot" id="chart-designer-modal-boxplot-button" role="button"
               class="btn btn-default" data-toggle="modal">Create box plot <span
                    class="glyphicon glyphicon-plus"></span></a>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div id="chart-view"></div>
        </div>
    </div>
</div>

<script>
    if (typeof Highcharts === 'undefined') {
        $.when(
                $.ajax("<@resource_href "/js/highstock-1.3.6/highstock.js"/>", {'cache': true})
        ).then(function () {
            $.when(
                    $.ajax("<@resource_href "/js/highstock-1.3.6/highcharts-more.js"/>", {'cache': true})
            );
        });
    }

    $.ajax("<@resource_href "/js/dataexplorer-charts.js"/>", {'cache': true});
</script>