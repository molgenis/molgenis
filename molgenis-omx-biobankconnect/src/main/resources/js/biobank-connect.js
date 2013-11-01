(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var CONTEXT_URL = null;
	var standardModal = new ns.StandardModal();
	var catalogueChooser = new ns.CatalogueChooser();
	var ontologyAnnotator = new ns.OntologyAnnotator();
	var mappingManager = new ns.MappingManager();

	ns.hrefToId = function(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	};
	
	ns.getCatalogueChooser = function() {
		return catalogueChooser;
	};
	
	ns.getOntologyAnnotator = function() {
		return ontologyAnnotator;
	};
	
	ns.getMappingManager = function() {
		return mappingManager;
	};
	
	ns.checkMatchingStatus = function(prefix, progressBarElement) {
		$.ajax({
			type : 'GET',
			url : prefix + '/match/status',
			contentType : 'application/json',
			success : function(response) {
				console.log(response);
				$('#control-container ul.pager li').addClass('disabled');
				if(response.isRunning){
					if(response.matchePercentage === 0){
						var width = $(progressBarElement).width();
						var parentWidth = $(progressBarElement).offsetParent().width();
						var percent = 100 * width / parentWidth + 4;
						$(progressBarElement).width(percent + '%');
					}else if(response.matchePercentage === 100){
						$(progressBarElement).width(response.matchePercentage + '%');
						setTimeout(function(){
							$(progressBarElement).empty().append('<p style="font-size:14px;padding-top:4px;">Storing result...</p>');
						}, 2000);
					}else{
						var width = $(progressBarElement).width();
						var parentWidth = $(progressBarElement).offsetParent().width();
						var percent = 100 * width / parentWidth + 5;
						if(percent < response.matchePercentage) percent = response.matchePercentage;
						$(progressBarElement).width(percent + '%').empty();
					}
					setTimeout(function(){
						ns.checkMatchingStatus(prefix, progressBarElement)
					}, 3000);
				}else {
					$(progressBarElement).empty().width('100%').parents('div:eq(0)').removeClass('active');
					$(progressBarElement).append('<p style="font-size:14px;padding-top:4px;">Finished!</p>');
					$('ul.pager li').removeClass('disabled');
				}
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	};
	
	ns.dataItemsTypeahead = function (type, dataSetId, query, response){
		var queryRules = [{
			field : 'type',
			operator : 'EQUALS',
			value : type,
		},{
			operator : 'AND'
		},{
			operator : 'SEARCH',
			value : query
		},{
			operator : 'LIMIT',
			value : 20
		}];
		var searchRequest = {
			documentType : 'protocolTree-' + dataSetId,
			queryRules : queryRules
		};
		
		searchApi.search(searchRequest, function(searchReponse){
			var result = [];
			var dataMap = {};
			$.each(searchReponse.searchHits, function(index, hit){
				var value = hit.columnValueMap.ontologyTerm;
				if($.inArray(value, result) === -1){
					var name = hit.columnValueMap.name;
					result.push(name);
					dataMap[name] = hit.columnValueMap;
				}
			});
			$(document).data('dataMap', dataMap);
			response(result);
		});
	};
	
	$(function(){
		var buttonGroup = $('.bwizard-buttons');
		$('#wizardForm').prepend(buttonGroup);
		buttonGroup.before('<legend />');
		$('.wizard-page').removeClass('well');
		$('div.wizard-page').css('min-height', 300);
		
		document.onkeydown = function(evt) {
		    evt = evt || window.event;
		    if (evt.keyCode == 27) {
		    	standardModal.closeModal();
		    }
		};
	});
}($, window.top.molgenis = window.top.molgenis || {}));
