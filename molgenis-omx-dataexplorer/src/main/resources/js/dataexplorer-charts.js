(function($, molgenis) {
	"use strict";
	molgenis.charts = molgenis.charts || {};
	var ns = molgenis.charts.dataexplorer = molgenis.charts.dataexplorer || {};
	var selectedFeaturesSelectOptions;
	
	ns.createXYLineChartRequestPayLoad = function (
			entity,
			x, 
			y, 
			xAxisLabel,
			yAxisLabel,
			width, 
			height, 
			title, 
			featureFilters,
			searchQuery, 
			searchRequest,
			queryRules) {
		
		return {
			"entity" : entity,
			"width": width,
			"height": height,
			"title": title,
			"queryRules": queryRules,
			"x": x,
			"y": y,
			"xAxisLabel": xAxisLabel,
			"yAxisLabel": yAxisLabel
			// TODO JJ
//			"featureFilters" : featureFilters,
//			"searchQuery" : searchQuery,
//			"searchRequest": searchRequest,
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
		
		console.log("features");
		console.log(features);
		
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
		$('#chart-designer-modal-button').click(function () {
			selectedFeaturesSelectOptions = null;
			$("#chart-select-xaxis-feature").empty();
			$("#chart-select-yaxis-feature").empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$("#chart-select-xaxis-feature").append(selectedFeaturesSelectOptions);
			$("#chart-select-yaxis-feature").append(selectedFeaturesSelectOptions);
		});
	});
	
//	ns.getSelectedFeatures = function() {
//		var tree = $('#feature-selection').dynatree('getTree');
//		
//		//console.log(tree);
//		
//		var features = $.map(tree.getSelectedNodes(), function(node) {
//			if(!node.data.isFolder){
//				console.log(node.data);
//				console.log(node.data.key);
//				return {node.data};
////				var uri = node.data.key;
////				return {feature: uri.substring(uri.lastIndexOf('/') + 1)};
//			}
//			return null;
//		});
//		
//		console.log("features");
//		console.log(features);
//		
//		return features;
//	};

//TODO Client selectie		
//	function getFeaturesSelectItems(data) {
//		var listItems = [];
//		
//		console.log(data);
//		
//		$.each(data, function (index) {		
//			listItems.push("<option value=''>" + data[index]selectFeatureForXAxis + "</option>");
//		});
//		return listItems.join('');
//	}
//	
//	$('#selectFeatureForXAxis').change(function () {
//		alert("selectFeatureForXAxis");
//	});
//	
//	$('#selectFeatureForXAxis').append(getFeaturesSelectItems(selectedFeatures));
	
})($, window.top.molgenis = window.top.molgenis || {});