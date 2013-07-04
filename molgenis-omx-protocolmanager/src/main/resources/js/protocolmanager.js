(function($, w) {
	"use strict";
	
	var ns = w.molgenis = w.molgenis || {};

	var resultsTable = null;
	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var currentPage = 1;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();

	// fill dataset select
	ns.fillDataSetSelect = function(callback) {
		restApi.getAsync('/api/v1/protocol', null, null, function(protocols) {
			var items = [];
			// TODO deal with multiple entity pages
			$.each(protocols.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.name + '</option>');
			});
			
			$('#protocol-select').html(items.join(''));
			$('#protocol-select').change(function() {
				
				ns.onProtocolSelectionChange($(this).val());
			});
			callback();
		});
	};



	ns.onProtocolSelectionChange = function(protocolUri) {
		// reset
		restApi.getAsync(protocolUri+'/features',null,null,function(features){
			$.each(features.items, function(key, val) {
				alert(val.href +' ' + val.description);
				
			});
		});
		
				
		
	};

	ns.download = function() {
		var jsonRequest = JSON.stringify(ns.createSearchRequest(false));
		
		parent.showSpinner();
		$.download('/plugin/protocolmanager/download',{searchRequest :  jsonRequest});
		parent.hideSpinner();
	};
	
	// on document ready
	$(function() {
		resultsTable = new ns.ResultsTable();

		$("#observationset-search").focus();
		$("#observationset-search").change(function(e) {
			ns.searchObservationSets($(this).val());
		});

		$('.feature-filter-dialog').dialog({
			modal : true,
			width : 500,
			autoOpen : false
		});
		
		$('#download-button').click(function() {
			ns.download();
		});
		
	});
}($, window.top));
