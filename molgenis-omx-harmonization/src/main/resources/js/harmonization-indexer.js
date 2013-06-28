(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	
	ns.searchAvailableIndices = function(runningIndexUri) {
		searchApi.search(ns.createSearchRequest(), function(searchResponse) {
			var searchHits = searchResponse.searchHits;
			if(searchHits.length > 0){
				$.each(searchHits, function(){
					var ontologyInfo = $(this)[0]["columnValueMap"];
					var ontologyUri = ontologyInfo.url;
					var status = "Indexed";
					if(runningIndexUri !== null){
						status = "Being indexed ...";
					}
					$('#ontology-table').append('<tr><td><a href="' + ontologyUri + '" target="_blank">' + ontologyUri + '</a></td><td>' + status + '</td></tr>');
				});
			}
		});
	};
	
	ns.createSearchRequest = function() {
		var queryRules = [];
		//todo: how to unlimit the search result
		queryRules.push({
			operator : 'LIMIT',
			value : 1000000
		});
		queryRules.push({
			operator : 'SEARCH',
			value : 'indexedOntology'
		});
		
		var searchRequest = {
			documentType : null,
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