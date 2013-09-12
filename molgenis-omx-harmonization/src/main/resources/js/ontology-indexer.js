(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var CONTEXT_URL = null;
	
	ns.setContextURL = function(CONTEXT_URL){
		this.CONTEXT_URL = CONTEXT_URL;
	};
	
	ns.getContextURL = function() {
		return this.CONTEXT_URL;
	};
	
	ns.searchAvailableIndices = function(runningIndexUri) {
		searchApi.search(ns.createSearchRequest(), function(searchResponse) {
			var searchHits = searchResponse.searchHits;
			if(searchHits.length > 0){
				$.each(searchHits, function(){
					var ontologyInfo = $(this)[0]["columnValueMap"];
					var ontologyUri = ontologyInfo.url;
					var ontologyName = ontologyInfo.ontologyLabel;
					var status = "Indexed";
					if(runningIndexUri !== null && ontologyUri === runningIndexUri){
						status = "Being indexed ...";
					}
					$('#ontology-table').append('<tr><td>' + ontologyName + '</td><td><a href="' + ontologyUri + '" target="_blank">' + ontologyUri + '</a></td><td>' + status + '</td></tr>');
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
	
	ns.showAlertMessage = function(message){
		var messageAlert = $('<div />').addClass('alert alert-error').append('<button type="button" class="close" data-dismiss="alert">&times;</button>');
		$('<span><strong>Message : </strong>' + message + '</span>').appendTo(messageAlert);
		$('#alert-message').append(messageAlert);
		w.setTimeout(function(){messageAlert.fadeOut(1000).remove()}, 10000);
	}
	
	$(function() {
		$('#index-button').click(function(){
			var ontologyName = $('#ontologyName').val();
			if(ontologyName == null || ontologyName == ''){
				ns.showAlertMessage('Please define the name of ontology!');
			}else if($('#uploadedOntology').val() !== ''){
				$('#ontologyindexer-form').attr({
					'action' : ns.getContextURL() + '/index',
					'method' : 'POST'
				}).submit();
			}else{
				ns.showAlertMessage('Please upload a file in OWL or OBO format!');
			}
		});
		$('#refresh-button').click(function(){
			$('#ontologyindexer-form').attr({
				'action' : ns.getContextURL(),
				'method' : 'GET'
			}).submit();
		});
	});
}($, window.top));