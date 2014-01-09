/**
 * This file will not work properly without loading first the dataexplorer.js file
 * 
 * @param $
 * @param molgenis
 */
		
(function($, molgenis) {
	"use strict";
	molgenis.charts = molgenis.charts || {};
	var ns = molgenis.charts.dataexplorer = molgenis.charts.dataexplorer || {};
	var restApi = new molgenis.RestClient();
	
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
			query,
			scale) {
		
		return {
			"entity": entity,
			"width": width,
			"height": height,
			"title": title,
			"type": "BOXPLOT_CHART",
			"observableFeature": featureIdentifier,
			"split": splitIdentifier,
			"query":query,
			"scale" : scale
		};
	};
	
// TODO heatmap	
//	ns.createHeatMapRequestPayLoad = function (
//			entity,
//			x, 
//			xAxisLabel,
//			width,
//			height,
//			title,
//			query) {
//		
//		return {
//			"entity": entity,
//			"width": width,
//			"height": height,
//			"title": title,
//			"y": x,
//			"yLabel":xAxisLabel,
//			"query":query
//		};
//	};
	
	/**
	 * retrieve select options items made from features objects
	 * Arguments:
	 * 1.	features: feature objects containing minimal the href and name properties.
	 */
	ns.getSelectedFeaturesSelectOptions = function(features) {
		var listItems = [];
		listItems.push('<option value='+ '-1' +'>select</option>');
		$.each(features, function (index) {
			listItems.push('<option value=' + features[index].href + '>' + features[index].name + '</option>');
		});
		return listItems.join('');
	};
	
	/**
	 * retrieve the selected features from the dynatree of the data-explorer.
	 */
	ns.getSelectedFeatures = function() {
		var tree = $('#feature-selection').dynatree('getTree');
		var selectedNodes = tree.getSelectedNodes();
		var selectedFeatures = [];
		var tempData;

		$.each(selectedNodes, function (index) {
			tempData = selectedNodes[index].data;
			if(!tempData.isFolder){
				selectedFeatures.push(ns.getFeatureByRestApi(tempData.key, restApi));
			}
			tempData = null;
		});
		return selectedFeatures;
	};
	
	/**
	 * filter the features based on the data type
	 * 
	 * Arguments:
	 * 1.	features:	the features to filter
	 * 2.	acceptableDataTypesList:	the acceptable data types
	 * 
	 * if the acceptableDataTypesList is empty or do'nt exist it return all of the features
	 */
	ns.filterFeatures = function(features, acceptableDataTypesList) {
		if(undefined === acceptableDataTypesList
				|| null === acceptableDataTypesList
				|| acceptableDataTypesList.length === 0) return features;

		var filterdFeatures = [];
		$.each(features, function (i) {
			$.each(acceptableDataTypesList, function (j) {
				if(features[i].dataType === acceptableDataTypesList[j]) {
					filterdFeatures.push(features[i]);
					return true;
				}
			});
		});
		return filterdFeatures;
	};
	
	/**
	 * handle the error and get the features through the rest Api
	 */
	ns.getFeatureByRestApi = function(value, restApi) {
		try
		{
			if(value === "-1"){
				return undefined;
			}
			return restApi.get(value);
		}
		catch (err) 
		{
			console.log(err);
			return undefined;
		}
	};
	
	/**
	 * make the scatter plot
	 */
	ns.makeScatterPlot = function (entity) {
		var xAxisFeature = ns.getFeatureByRestApi($('#scatterplot-select-xaxis-feature').val(), restApi);
		var xAxisDataType;
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
			xAxisDataType = xAxisFeature.dataType;
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
				$('#tabs a:last').tab('show');
				if(xAxisDataType === "date" || xAxisDataType === "datetime") {
					$('#chart-view').highcharts('StockChart', options);
				} else {
					$('#chart-view').highcharts(options);
				}
			}
		});
		
	};
	
	//Box Plot
	ns.makeBoxPlot = function (entity) {
		var feature = ns.getFeatureByRestApi($('#boxplot-select-feature').val(), restApi);
		var splitFeature = ns.getFeatureByRestApi($('#boxplot-select-split-feature').val(), restApi);
		var title = $('#boxplot-title').val();
		var width = 1024;
		var height = 576;
		var searchRequest = molgenis.createSearchRequest();
		var query = searchRequest.query;
		var featureIdentifier, splitIdentifier;
		var scale;
		
		if($('#boxplot-scale').val() === "") {
			scale = 1.5; // Default value
		} else {
			scale = new Number($('#boxplot-scale').val())
		}
		
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
					query,
					scale
			)),
			contentType : "application/json; charset=utf-8",
			cache: false,
			async: true,
			success : function(options){
				$('#tabs a:last').tab('show');
			 	$('#chart-view').highcharts(options);
			}
		});
		
	};

// TODO heatmap
//
//	ns.makeHeatMap = function (entity) {
//		var xAxisFeature = ns.getFeatureByRestApi($('#heatmap-select-xaxis-feature').val(), restApi);
//		var width = 1024;
//		var height = 576; 
//		var title = $('#heatmap-title').val();
//		var searchRequest = molgenis.createSearchRequest();
//		var query = searchRequest.query;
//		var x, xAxisLabel;
//		
//		if(xAxisFeature) {
//			x = xAxisFeature.identifier;
//			xAxisLabel = xAxisFeature.name;
//		} 
//		
//		$.ajax({
//			type : "POST",
//			url : "/charts/heatmap",
//			data : JSON.stringify(molgenis.charts.dataexplorer.createHeatMapRequestPayLoad(
//					entity,
//					x, 
//					xAxisLabel,
//					width,
//					height,
//					title,
//					query
//			)),
//			contentType : "application/json; charset=utf-8",
//			cache: false,
//			async: true,
//			success : function(response){
//				alert(response);
//			}
//		});	
//	};
	
	$(function() {
		/****
		 * all dataTypes:
		 * 	"html", "mref", "xref", "email", "hyperlink", "text", "string", "bool", "categorical"
		 * 	"date", "datetime"
		 * 	"long", "integer", "int", "decimal"
		 ****/

		$('#chart-designer-modal-scatterplot-button').click(function () {
			var allSelectedFeatures = ns.getSelectedFeatures();
			
			$('#scatterplot-select-xaxis-feature').empty();
			$('#scatterplot-select-yaxis-feature').empty();
			$('#scatterplot-select-split-feature').empty();	
			
			$('#scatterplot-select-xaxis-feature').append(
					ns.getSelectedFeaturesSelectOptions(ns.filterFeatures(allSelectedFeatures, ["decimal", "long", "integer", "int", "date", "datetime"])));
			$('#scatterplot-select-yaxis-feature').append(
					ns.getSelectedFeaturesSelectOptions(ns.filterFeatures(allSelectedFeatures, ["decimal", "long", "integer", "int", "date", "datetime"])));
			$('#scatterplot-select-split-feature').append(
					ns.getSelectedFeaturesSelectOptions(ns.filterFeatures(allSelectedFeatures)));
		});
		
		$('#chart-designer-modal-boxplot-button').click(function () {
			var allSelectedFeatures = ns.getSelectedFeatures();
			
			$('#boxplot-select-feature').empty();
			$('#boxplot-select-split-feature').empty();
			
			$('#boxplot-select-feature').append(
					ns.getSelectedFeaturesSelectOptions(ns.filterFeatures(allSelectedFeatures, ["decimal", "long", "integer", "int"])));
			$('#boxplot-select-split-feature').append(
					ns.getSelectedFeaturesSelectOptions(ns.filterFeatures(allSelectedFeatures)));
		});

// TODO Heatmap		
//		$('#chart-designer-modal-heatmap-button').click(function () {
//			var selectedFeaturesSelectOptions = null;
//			$('#heatmap-select-xaxis-feature').empty();
//			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
//			$('#heatmap-select-xaxis-feature').append(selectedFeaturesSelectOptions);
//		});
		
		// fire event handler
		$('#dataset-select').change();
		
		$('#scatterplot-designer-modal-create-button').click(function(){
			molgenis.charts.dataexplorer.makeScatterPlot(molgenis.getSelectedDataSetIdentifier());
		});
		$('#boxplot-designer-modal-create-button').click(function(){
			molgenis.charts.dataexplorer.makeBoxPlot(molgenis.getSelectedDataSetIdentifier());
		});
		// TODO heat map
		//$('#heatmap-designer-modal-create-button').click(function(){
		//	molgenis.charts.dataexplorer.makeHeatMap(molgenis.getSelectedDataSetIdentifier());
		//});
	});
	
})($, window.top.molgenis = window.top.molgenis || {});