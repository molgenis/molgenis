(function($, molgenis) {
	"use strict";
	molgenis.charts = molgenis.charts || {};
	var ns = molgenis.charts.dataexplorer = molgenis.charts.dataexplorer || {};
	var selectedFeaturesSelectOptions;
	
	ns.createScatterPlotChartRequestPayLoad = function (
			entity,
			x, 
			y, 
			xAxisLabel,
			yAxisLabel,
			width, 
			height, 
			title,
			query,
			splitFeature) {
		
		return {
			"entity" : entity,
			"width": width,
			"height": height,
			"title": title,
			"type": "SCATTER_CHART",
			"query": query,
			"x": x,
			"y": y,
			"xAxisLabel": xAxisLabel,
			"yAxisLabel": yAxisLabel,
			"split": splitFeature
		};
	};
	
	ns.createBoxPlotChartRequestPayLoad = function (
			entity,
			featureIdentifier) {
		
		return {
			"entity" : entity,
			"type" : "BOXPLOT_CHART",
			"observableFeature": featureIdentifier
		};
	};
	
	ns.getSelectedFeatures = function() {
		var tree = $('#feature-selection').dynatree('getTree');
		var features = $.map(tree.getSelectedNodes(), function(node) {
			if(!node.data.isFolder){
				return {feature: node.data};
			}
			return null;
		});
		
		return features;
	};
	
	ns.getSelectedFeaturesSelectOptions = function() {
		var tree = $('#feature-selection').dynatree('getTree');
		var selectedNodes = tree.getSelectedNodes();
		var listItems = [];
		var tempData;
		listItems.push("<option value='-1'></option>");

		$.each(selectedNodes, function (index) {
			tempData = selectedNodes[index].data;
			console.log(tempData);
			if(!tempData.isFolder){
				listItems.push("<option value=" + tempData.key + ">" + tempData.title + "</option>");
			}
			tempData = null;
		});
		
		return listItems.join('');
	};
	
	$(function() {
		$('#chart-designer-modal-scatterplot-button').click(function () {
			selectedFeaturesSelectOptions = null;
			$("#scatterplot-select-xaxis-feature").empty();
			$("#scatterplot-select-yaxis-feature").empty();
			$("#scatterplot-select-split-feature").empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$("#scatterplot-select-xaxis-feature").append(selectedFeaturesSelectOptions);
			$("#scatterplot-select-yaxis-feature").append(selectedFeaturesSelectOptions);
			$("#scatterplot-select-split-feature").append(selectedFeaturesSelectOptions);
		});
		
		$('#chart-designer-modal-boxplot-button').click(function () {
			selectedFeaturesSelectOptions = null;
			$("#boxplot-select-feature").empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$("#boxplot-select-feature").append(selectedFeaturesSelectOptions);
		});
	});
	
})($, window.top.molgenis = window.top.molgenis || {});