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
			type,
			query) {
		
		return {
			"entity" : entity,
			"width": width,
			"height": height,
			"title": title,
			"type": type,
			"query": query,
			"x": x,
			"y": y,
			"xAxisLabel": xAxisLabel,
			"yAxisLabel": yAxisLabel
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
		$('#chart-designer-modal-button').click(function () {
			selectedFeaturesSelectOptions = null;
			$("#chart-select-xaxis-feature").empty();
			$("#chart-select-yaxis-feature").empty();
			selectedFeaturesSelectOptions = ns.getSelectedFeaturesSelectOptions();
			$("#chart-select-xaxis-feature").append(selectedFeaturesSelectOptions);
			$("#chart-select-yaxis-feature").append(selectedFeaturesSelectOptions);
		});
	});
	
})($, window.top.molgenis = window.top.molgenis || {});