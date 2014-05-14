(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	
	ns.searchAvailableIndices = function(runningIndexUri) {
		searchApi.search(ns.createSearchRequest(), function(searchResponse) {
			var searchHits = searchResponse.searchHits;
			if(searchHits.length > 0){
				var table = $('#ontology-table');
				$.each(searchHits, function(){
					var ontologyInfo = $(this)[0]["columnValueMap"];
					var ontologyUri = ontologyInfo.ontologyIRI;
					var ontologyName = ontologyInfo.ontologyLabel;
					var status = "Indexed";
					if(runningIndexUri !== null && ontologyUri === runningIndexUri){
						status = "Being indexed ...";
					}
					var eachRow = $('<tr />');
					$('<td />').append(ontologyName).appendTo(eachRow);
					$('<td />').append('<a href="' + ontologyUri + '" target="_blank">' + ontologyUri + '</a>').appendTo(eachRow);
					$('<td />').append(status).appendTo(eachRow);
					var removeIcon = $('<i class="icon-remove"></i>').click(function(){
						$('input[name="ontologyUri"]').val(ontologyUri);
						$('#ontologyindexer-form').attr({
							'action' : molgenis.getContextUrl() + '/remove',
							'method' : 'POST'
						}).submit();
					});
					$('<td />').append(removeIcon).appendTo(eachRow);
					eachRow.appendTo(table);
				});
			}
		});
	};
	
	ns.createSearchRequest = function() {
		
		var searchRequest = {
			documentType : null,
			query : {
				pageSize: 1000000,
				rules : [[{
					operator : 'SEARCH',
					value : 'indexedOntology'
				}]]
			}
		};
		return searchRequest;
	};
	
	ns.showAlertMessage = function(alertClass, message){
		var messageDiv = $('#alert-message');
		if(messageDiv.length === 0) messageDiv = $('<div id="alert-message"></div>');
		var messageAlert = $('<div />').addClass(alertClass).append('<button type="button" class="close" data-dismiss="alert">&times;</button>');
		$('<span><strong>Message : </strong>' + message + '</span>').appendTo(messageAlert);
		messageDiv.empty().append(messageAlert);
		$('form:eq(-1)').prepend(messageDiv);
		w.setTimeout(function(){messageDiv.fadeOut(1000).remove()}, 5000);
	};
	
	$(function() {
		$('#index-button').click(function(){
			var ontologyName = $('#ontologyName').val();
			if(ontologyName == null || ontologyName == ''){
				ns.showAlertMessage('alert alert-error','Please define the name of ontology!');
			}else if($('#uploadedOntology').val() !== ''){
				$('#ontologyindexer-form').attr({
					'action' : molgenis.getContextUrl() + '/index',
					'method' : 'POST'
				}).submit();
			}else{
				ns.showAlertMessage('Please upload a file in OWL or OBO format!');
			}
		});
		$('#refresh-button').click(function(){
			$('#ontologyindexer-form').attr({
				'action' : molgenis.getContextUrl(),
				'method' : 'GET'
			}).submit();
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));