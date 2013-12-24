(function($, molgenis) {
	"use strict";
	molgenis.charts = molgenis.charts || {};
	var ns = molgenis.charts.dataexplorer = molgenis.charts.dataexplorer || {};
	
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
			width, 
			height,
			title,
			featureIdentifier,
			splitIdentifier,
			query) {
		
		return {
			"entity": entity,
			"width": width,
			"height": height,
			"title": title,
			"type": "BOXPLOT_CHART",
			"observableFeature": featureIdentifier,
			"split": splitIdentifier,
			"query":query,
			"multiplyIQR" : 0.1
		};
	};
	
	ns.createHeatMapRequestPayLoad = function (
			entity,
			x, 
			xAxisLabel,
			width,
			height,
			title,
			query) {
		
		return {
			"entity": entity,
			"width": width,
			"height": height,
			"title": title,
			"y": x,
			"yLabel":xAxisLabel,
			"query":query
		};
	};
	
	ns.getSelectedFeaturesSelectOptions = function() {
		var tree = $('#feature-selection').dynatree('getTree');
		var selectedNodes = tree.getSelectedNodes();
		var listItems = [];
		var tempData;
		listItems.push('<option value='+ '-1' +'>select</option>');
		$.each(selectedNodes, function (index) {
			tempData = selectedNodes[index].data;
			if(!tempData.isFolder){
				listItems.push('<option value=' + tempData.key + '>' + tempData.title + '</option>');
			}
			tempData = null;
		});
		
		return listItems.join('');
	};
	
	ns.getFeatureByRestApi = function(value,restApi) {
		try
		{
			return restApi.get(value);
		}
		catch (err) 
		{
			console.log(err);
			return undefined;
		}
	};
	
	//Scatter Plot
	ns.makeScatterPlotChartRequest = function (entity, restApi) {
		var xAxisFeature = ns.getFeatureByRestApi($('#scatterplot-select-xaxis-feature').val(), restApi);
		var yAxisFeature = ns.getFeatureByRestApi($('#scatterplot-select-yaxis-feature').val(), restApi);
		var splitFeature = ns.getFeatureByRestApi($('#scatterplot-select-split-feature').val(), restApi);
		var width = 1024;
		var height = 576; 
		var title = $('#scatterplot-title').val();
		var searchRequest = molgenis.createSearchRequest();
		var query = searchRequest.query;
		var x, y, xAxisLabel, yAxisLabel, split;
		
		if(xAxisFeature) {
			x = xAxisFeature.identifier;
			xAxisLabel = xAxisFeature.name;
		} 
		
		if(yAxisFeature) {
			y = yAxisFeature.identifier;
			yAxisLabel = yAxisFeature.name;
		}
		
		if(splitFeature) {
			split = splitFeature.identifier;
		}
		
		$.ajax({
			type : "POST",
			url : "/charts/xydatachart",
			data : JSON.stringify(molgenis.charts.dataexplorer.createScatterPlotChartRequestPayLoad(
					entity,
					x, 
					y, 
					xAxisLabel,
					yAxisLabel,
					width,
					height,
					title,
					query,
					split
			)),
			contentType : "application/json; charset=utf-8",
			cache: false,
			async: true,
			success : function(options){
				console.log(options);
				$('#tabs a:last').tab('show');
			 	$('#chart-container').highcharts(options);
				/**
				 * TODO implement type chart detection
				 * $('#chart-container').highcharts('StockChart', options);
				 */
			}
		});
		
	};
	
	//Box Plot
	ns.makeBoxPlotChartRequest = function (entity, restApi) {
		var feature = ns.getFeatureByRestApi($('#boxplot-select-feature').val(), restApi);
		var splitFeature = ns.getFeatureByRestApi($('#boxplot-select-split-feature').val(), restApi);
		var title = $('#boxplot-title').val();
		var width = 1024;
		var height = 576;
		var searchRequest = molgenis.createSearchRequest();
		var query = searchRequest.query;
		var featureIdentifier, splitIdentifier;
		
		if(feature) {
			featureIdentifier = feature.identifier;		
		}
		
		if(splitFeature) {
			splitIdentifier = splitFeature.identifier;
		}
		
		$.ajax({
			type : "POST",
			url : "/charts/boxplot",
			data : JSON.stringify(molgenis.charts.dataexplorer.createBoxPlotChartRequestPayLoad(
					entity,
					width,
					height,
					title,
					featureIdentifier,
					splitIdentifier,
					query
			)),
			contentType : "application/json; charset=utf-8",
			cache: false,
			async: true,
			success : function(options){
				console.log(options);
				$('#tabs a:last').tab('show');
			 	$('#chart-container').highcharts(options);
			}
		});
		
	};
	
	//Heatmap
	ns.makeHeatMapChartRequest = function (entity, restApi) {
		var xAxisFeature = ns.getFeatureByRestApi($('#heatmap-select-xaxis-feature').val(), restApi);
		var width = 1024;
		var height = 576; 
		var title = $('#heatmap-title').val();
		var searchRequest = molgenis.createSearchRequest();
		var query = searchRequest.query;
		var x, xAxisLabel;
		
		if(xAxisFeature) {
			x = xAxisFeature.identifier;
			xAxisLabel = xAxisFeature.name;
		} 
		
		$.ajax({
			type : "POST",
			url : "/charts/heatmap",
			data : JSON.stringify(molgenis.charts.dataexplorer.createHeatMapRequestPayLoad(
					entity,
					x, 
					xAxisLabel,
					width,
					height,
					title,
					query
			)),
			contentType : "application/json; charset=utf-8",
			cache: false,
			async: true,
			success : function(response){
				alert(response);
			}
		});
		
	};
	
	$(function() {
		$('#chart-designer-modal-scatterplot-button').click(function () {
			var selectedFeaturesSelectOptions = null;
			$('#scatterplot-select-xaxis-feature').empty();
			$('#scatterplot-select-yaxis-feature').empty();
			$('#scatterplot-select-split-feature').empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$('#scatterplot-select-xaxis-feature').append(selectedFeaturesSelectOptions);
			$('#scatterplot-select-yaxis-feature').append(selectedFeaturesSelectOptions);
			$('#scatterplot-select-split-feature').append(selectedFeaturesSelectOptions);
		});
		
		$('#chart-designer-modal-boxplot-button').click(function () {
			var selectedFeaturesSelectOptions = null;
			$('#boxplot-select-feature').empty();
			$('#boxplot-select-split-feature').empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$('#boxplot-select-feature').append(selectedFeaturesSelectOptions);
			$('#boxplot-select-split-feature').append(selectedFeaturesSelectOptions);
		});
		
		$('#chart-designer-modal-heatmap-button').click(function () {
			var selectedFeaturesSelectOptions = null;
			$('#heatmap-select-xaxis-feature').empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$('#heatmap-select-xaxis-feature').append(selectedFeaturesSelectOptions);
		});
	});
	
})($, window.top.molgenis = window.top.molgenis || {});