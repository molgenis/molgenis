(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	
	ns.searchAvailableIndices = function() {
		searchApi.search(ns.createSearchRequest(), function(searchResponse) {
			console.log(searchResponse);
		});
	};
	
	ns.createSearchRequest = function() {
		var queryRules = [];
		//todo: how to unlimit the search result
		queryRules.push({
			operator : 'LIMIT',
			value : 1000000
		});
		
		var searchRequest = {
			documentType : "protocolTree-51008",
			queryRules : queryRules
		};
		return searchRequest;
	};
	
	ns.indexerApi = function(callback) {
		$.ajax({
			type : 'POST',
			url : '/plugin/ontologyindexer?_method=GET',
			data : JSON.stringify({
				
			}),
			contentType : 'application/json',
			async : false,
			success : function(entities) {
				callback(entities);
			}
		});
	};
	
	$(function() {
		$('#index-button').click(function(){
			$('input[name="__action"]').val("indexOntology");
			$('#harmonizationIndexer-form').submit();
		});
	});
}($, window.top));