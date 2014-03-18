<#include "/charts-forms/view-scatterplot.ftl" parse=false>
<#include "/charts-forms/view-boxplot.ftl" parse=false>
<#include "/charts-forms/view-heatmap.ftl" parse=false>
<div class="row-fluid">
	<div class="btn-group" class="span9">
		<a href="#chart-designer-modal-scatterplot" id="chart-designer-modal-scatterplot-button" role="button" class="btn" data-toggle="modal">Create scatter plot <i class="icon-plus"></i></a>
		<a href="#chart-designer-modal-boxplot" id="chart-designer-modal-boxplot-button" role="button" class="btn" data-toggle="modal">Create box plot <i class="icon-plus"></i></a>
	</div>
</div>
<div class="row-fluid">
	<div id="chart-view" class="span9"></div>
</div>
<script>
	$.when(
		$.ajax("/js/highstock-1.3.6/highstock.js", {'cache': true}),
		$.ajax("/js/highstock-1.3.6/highcharts-more.js", {'cache': true}),
		$.ajax("/js/dataexplorer-charts.js"), {'cache': true})
		.then(function() {});
</script>