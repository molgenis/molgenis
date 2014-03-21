(function($, molgenis, w) {
	"use strict";
	
	var searchApi = new molgenis.SearchClient();
	var standardModal = new molgenis.StandardModal();
	
	molgenis.hrefToId = function(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	};
	
	molgenis.showMessage = function(alertClass, message, parentDiv){
		var messageDiv = $('#alert-message');
		if(messageDiv.length === 0) messageDiv = $('<div id="alert-message"></div>').addClass('span12');
		var button = $('<button type="button" class="close" data-dismiss="alert">&times;</button>');
		messageDiv.empty().addClass(alertClass).css('margin-left', '-1px').append(button);
		$('<span><strong>Message : </strong>' + message + '</span>').appendTo(messageDiv);
		w.setTimeout(function(){messageDiv.fadeOut(1000).remove()}, 10000);
		button.click(function(){
			messageDiv.remove();
		});
		parentDiv.prepend(messageDiv);
	}
	
	molgenis.i18nDescription = function(feature){
		if(feature.description === undefined) feature.description = '';
		if(feature.description.indexOf('{') !== 0){
			feature.description = '{"en":"' + (feature.description === null ? '' : feature.description.replace(new RegExp('"','gm'), '')) +'"}';
		}
		return eval('(' + feature.description + ')');
	};
	
	molgenis.ontologyMatcherRunning = function (callback, contextUrl) {
		if(contextUrl === undefined || contextUrl === null) contextUrl = molgenis.getContextUrl();
		$.ajax({
			type : 'GET',
			url : contextUrl + '/running',
			contentType : 'application/json',
			success : function(response) {
				if(response.isRunning){
					var childElements = $('#wizardForm').children();
					if($('#wizardForm').data('childElements') === null || $('#wizardForm').data('childElements') === undefined)
						$('#wizardForm').data('childElements', childElements);
					var items = [];
					items.push('<br><div class="row-fluid"><div class="offset2 span1"><strong>Message </strong></div>');
					items.push('<div class="offset1"><p>other user is currently running BiobankConnect using the same account, please be patient or login as another user!</p></div></div>');
					$('#wizardForm').html(items.join(''));
					setTimeout(function(){
						molgenis.ontologyMatcherRunning(callback, contextUrl);
					}, 5000);
				}else{
					var childElements = $('#wizardForm').data('childElements');
					$('#wizardForm').data('childElements', null);
					if(childElements !== null && childElements !== undefined) $('#wizardForm').empty().append(childElements);
					$('ul.pager a').on('click', function(e) {
						e.preventDefault();
						if (!$(this).parent().hasClass('disabled')) {
							$('#wizardForm').attr('action', $(this).attr('href')).submit();
						}
						
						return false;
					});
					if(callback !== undefined && callback !== null) {
						callback();
					}
				}
			}
		});
	};
	
	molgenis.checkMatchingStatus = function(contextUrl, parentElement, currentStatus, prevStage) {
		$.ajax({
			type : 'GET',
			url : contextUrl + '/match/status',
			contentType : 'application/json',
			success : function(response){
				if(response.isRunning){
					currentStatus[response.stage].show();
					var progressBar = currentStatus[response.stage].children('.progress:eq(0)');					
					var width = $(progressBar).find('.bar:eq(0)').width();
					var parentWidth = $(progressBar).find('.bar:eq(0)').parent().width();
					var percent = (100 * width / parentWidth) + (1 / response.totalUsers);
                    if(percent < response.matchePercentage) percent = response.matchePercentage;
                    progressBar.find('div.bar:eq(0)').width((percent > 100 ? 100 : percent) + '%');
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
					if(response.totalUsers > 1){
						var warningDiv = null;
						if($('#other-user-alert').length > 0) warningDiv = $('#other-user-alert');
						else warningDiv = $('<div id="other-user-alert" class="row-fluid" style="margin-bottom:10px;"></div>');
						warningDiv.empty().append('<div class="span12"><span style="display: block;font-size:16px;text-align:center;">Other users are using BiobankConnect, it might slow down the process. Please be patient!</span></div>');
						parentElement.find('.progress:eq(0)').parents('div:eq(0)').before(warningDiv);
					}else{
						$('#other-user-alert').remove();
					}
					setTimeout(function(){
						molgenis.checkMatchingStatus(contextUrl, parentElement, currentStatus)
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
	
	molgenis.dataItemsTypeahead = function (type, dataSetId, query, response){
		var queryRules = [{
			field : 'type',
			operator : 'EQUALS',
			value : type,
		},{
			operator : 'AND'
		},{
			operator : 'SEARCH',
			value : query
		}];
		
		var searchRequest = {
			documentType : 'protocolTree-' + dataSetId,
			query:{
				pageSize: 20,
				rules:[queryRules]
			}
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
	
	molgenis.ontologyTermTypeahead = function (field, query, response){
		var queryRules = [{
			field : field,
			operator : 'LIKE',
			value : query,
		},{
			operator : 'OR'
		},{
			field : field,
			operator : 'EQUALS',
			value : query,
		}];
		var searchRequest = {
			documentType : null,
			query : {
				pageSize: 40,
				rules: [queryRules]
			}
		};
		searchApi.search(searchRequest, function(searchReponse){
			var result = [];
			var dataMap = {};
			$.each(searchReponse.searchHits, function(index, hit){
				var ontologyName = hit.columnValueMap.ontologyLabel;
				var termName = hit.columnValueMap.ontologyTermSynonym;
				termName = ontologyName === '' ? termName : ontologyName + ':' + termName;
				if($.inArray(termName, result) === -1){					
					result.push(termName);
					dataMap[termName] = hit.columnValueMap;
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
}($, window.top.molgenis = window.top.molgenis || {}, window.top));