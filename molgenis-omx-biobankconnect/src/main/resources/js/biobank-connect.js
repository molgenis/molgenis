(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var CONTEXT_URL = null;
	var isRunning = null;
	var standardModal = new ns.StandardModal();
	var catalogueChooser = new ns.CatalogueChooser();
	var ontologyAnnotator = new ns.OntologyAnnotator();
	var mappingManager = new ns.MappingManager();

	ns.setContextURL = function(CONTEXT_URL){
		this.CONTEXT_URL = CONTEXT_URL;
	};
	
	ns.getContextURL = function(){
		return this.CONTEXT_URL;
	};
	
	ns.setIsRunning = function(isRunning){
		this.isRunning = isRunning;
	};
	
	ns.getIsRunning = function(){
		return this.isRunning;
	};
	
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
	
	ns.ontologyMatcherRunning = function(callback) {
		$.ajax({
			type : 'GET',
			url : ns.getContextUrl() + '/running',
			contentType : 'application/json',
			success : function(response) {
				if(response.isRunning){
					var childElements = $('#wizardForm').children();
					if($('#wizardForm').data('childElements') === null || $('#wizardForm').data('childElements') === undefined)
						$('#wizardForm').data('childElements', childElements);
					var items = [];
					items.push('<br><div class="row-fluid"><div class="offset2 span1"><strong>Message </strong></div>');
					items.push('<div class="offset1"><p>other user is currently running BiobankConnect using the same account, please be patient</p></div></div>');
					$('#wizardForm').html(items.join(''));
					setTimeout(function(){
						ns.ontologyMatcherRunning(callback);
					}, 5000);
				}else{
					var childElements = $('#wizardForm').data('childElements');
					$('#wizardForm').data('childElements', null);
					if(childElements !== null && childElements !== undefined) $('#wizardForm').empty().append(childElements);
					if(callback !== undefined && callback !== null) {
						callback();
					}
				}
			},
			error : function(error){
				console.log('error');
			}
		});
	};
	
	ns.checkMatchingStatus = function(contextUrl, parentElement, currentStatus, prevStage) {
		$.ajax({
			type : 'GET',
			url : contextUrl + '/match/status',
			contentType : 'application/json',
			success : function(response){
				if(response.isRunning){
					currentStatus[response.stage].show();
					var progressBar = currentStatus[response.stage].children('.progress:eq(0)');					
					var width = $(progressBar).find('.bar:eq(0)').width();
					var parentWidth = $(progressBar).find('.bar:eq(0)').offsetParent().width();
					var percent = 100 * width / parentWidth + 2;
                    if(percent < response.matchePercentage || percent > 100) percent = response.matchePercentage;
                    progressBar.find('div.bar:eq(0)').width(percent + '%');
					
					if(prevStage === undefined || prevStage === null || prevStage !== response.stage){
						prevStage = response.stage;
						$.each(currentStatus, function(stageName, progressBar){
							if(stageName === response.stage) return false;
							progressBar.show();
							var innerProgressBar = progressBar.find('div.bar:eq(0)');
							$(innerProgressBar).width('100%').parents('div:eq(0)').removeClass('active');
							$(innerProgressBar).append('<p style="font-size:14px;padding-top:4px;">Finished!</p>');
						});
					}
					
					if(response.otherUsers){
						var warningDiv = null;
						if($('#other-user-alert').length > 0) warningDiv = $('#other-user-alert');
						else warningDiv = $('<div id="other-user-alert" class="row-fluid" style="margin-bottom:10px;"></div>');
						warningDiv.empty().append('<div class="span12"><span style="display: block;font-size:16px;text-align:center;">Other users are using BiobankConnect, it might slow down the process. Please be patient!</span></div>');
						parentElement.find('.progress:eq(0)').parents('div:eq(0)').before(warningDiv);
					}else{
						$('#other-user-alert').remove();
					}
					
					setTimeout(function(){
						ns.checkMatchingStatus(contextUrl, parentElement, currentStatus)
					}, 3000);
				}else {
					$.each(currentStatus, function(stageName, progressBar){
						progressBar.show();
						var innerProgressBar = progressBar.find('div.bar:eq(0)');
						$(innerProgressBar).width('100%').parents('div:eq(0)').removeClass('active');
						$(innerProgressBar).append('<p style="font-size:14px;padding-top:4px;">Finished!</p>');
					});
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
