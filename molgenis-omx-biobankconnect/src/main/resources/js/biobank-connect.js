(function($, molgenis, w) {
	"use strict";
	
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var nrItemsPerPage = 10;
	
	molgenis.getTotalNumberOfItems = function(dataSetId){
		var number = 0;
		var request = {
			'dataSetId' : dataSetId,  
			'query' : {
				'pageSize' : 100000,
			}
		};
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/allattributes',
			async : false,
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data){
				number = data.searchHits.length;
			}
		});
		return number;
	};
	
	molgenis.createMatrixForDataItems = function(options){
		var request = {
			'dataSetId' : options.dataSetId, 
			'queryString' : options.queryText, 
			'query' : {
				'offset' : options.page ? options.page.start : 0 ,
				'pageSize' : nrItemsPerPage,
				'sort' : options.sortRule
			}
		};
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/allattributes',
			async : false,
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data, textStatus, request){
				if(data.searchHits){
					var searchHits = data.searchHits;
					var table = $('<table />').addClass('table table-bordered');
					options.container.find('table').remove();
					options.container.prepend(table);
					var body = $('<tbody />');
					table.append(createTableHeader(options)).append(body);
					if(options.createTableRow === undefined || options.createTableRow === null){
						options.createTableRow = createTableRow;
					}
					$.each(searchHits, function(){
						$(options.createTableRow($(this)[0]['columnValueMap'], table)).appendTo(body);
					});
					initPager(options, data.totalHitCount);
					
				}else{
					molgenis.createAlert([{'message' : 'There is no dataset available!'}], 'warning', $('#wizardForm'));
				}
			}		
		});
		
		function initPager(options, totalHitCount){
			if(options.updatePager || options.container.find('#pager-' + options.dataSetId).length === 0){
				options.updatePager = false;
				var tablePager = $('<div/>').attr('id', 'pager-' + options.dataSetId);
				options.container.find('div.pagination').remove();
				options.container.append(tablePager);
				tablePager.pager({
					'nrItems' : totalHitCount,
					'nrItemsPerPage' : nrItemsPerPage,
					'onPageChange' : function(page) {
						options.page = page;
						molgenis.createMatrixForDataItems(options);
					}
				});
			}
		}
		
		function createTableHeader(options){
			var headerRow = $('<tr />');
			if(options.tableHeaders.length > 0){
				var firstColumnWidth = 30;
				var firstColumn = $('<th>' + options.tableHeaders[0] + '</th>').css('width', firstColumnWidth + '%').appendTo(headerRow);
				if(options.sortRule){
					if (options.sortRule.orders[0].direction == 'ASC') {
						$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-s down float-right"></span>').appendTo(firstColumn);
					} else {
						$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-n up float-right"></span>').appendTo(firstColumn);
					}
				} else {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-2-n-s updown float-right"></span>').appendTo(firstColumn);
				}
				var columnWidth = (100 - firstColumnWidth) /options.tableHeaders.length;
				$.each(options.tableHeaders, function(index, eachHeader){
					if(index > 0){
						$('<th />').append(eachHeader).css('width', columnWidth + '%').appendTo(headerRow);
					}
				});
				// Sort click
				$(firstColumn).find('.ui-icon').click(function(e) {
					e.preventDefault();
					if (options.sortRule && options.sortRule.orders[0].direction == 'ASC') {
						options.sortRule = {
							orders: [{
								property: 'name',
								direction: 'DESC'
							}]
						};
					} else {
						options.sortRule = {
							orders: [{
								property: 'name',
								direction: 'ASC'
							}]
						};
					}
					molgenis.createMatrixForDataItems(options);
				});
			}
			return headerRow;
		}
		
		function createTableRow(feature){
			var row = $('<tr />');
			var description = feature.description;
			var isPopOver = description.length < 120;
			var descriptionSpan = $('<span />').html(isPopOver ? description : description.substring(0, 120) + '...');
			if(!isPopOver){
				descriptionSpan.addClass('show-popover');
				descriptionSpan.popover({
					content : description,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			var featureNameSpan = $('<span>' + feature.name + '</span>');
			$('<td />').append(featureNameSpan).appendTo(row);
			$('<td />').append(descriptionSpan).appendTo(row);
			return row;
		}
	};
	
	molgenis.hrefToId = function(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	};
	
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
	
	molgenis.getFeatureFromIndex = function (feature, callback){		
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/attribute',
			async : false,
			data : JSON.stringify(molgenis.hrefToId(feature.href)),
			contentType : 'application/json',
			success : function(data, textStatus, request){
				$.each(data.searchHits, function(index, hit){
					callback(hit);
					return false;
				});
			}		
		});
	};
	
	molgenis.dataItemsTypeahead = function (dataSetId, query, response, approximate){
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/allattributes',
			async : false,
			data : JSON.stringify({'dataSetId' : dataSetId, 'queryString' : query, 'approximate' : (approximate !== undefined && approximate !== null)}),
			contentType : 'application/json',
			success : function(data, textStatus, request){
				var result = [];
				var dataMap = {};
				$.each(data.searchHits, function(index, hit){
					var value = hit.columnValueMap.name;
					if($.inArray(value, result) === -1){
						var name = hit.columnValueMap.name;
						result.push(name);
						dataMap[name] = hit.columnValueMap;
					}
				});
				$(document).data('dataMap', dataMap);
				response(result);
			}		
		});
	};
	
	molgenis.ontologyTermTypeahead = function (field, query, response){
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/ontologyterm',
			async : false,
			data : JSON.stringify({'queryString' : query}),
			contentType : 'application/json',
			success : function(data, textStatus, request){
				var result = [];
				var dataMap = {};
				$.each(data.searchHits, function(index, hit){
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
			}		
		});
	};
	
	molgenis.adaptContextUrl = function (){
		return molgenis.getContextUrl().replace(/biobankconnect$/g, "algorithm");
	};
	
	$(function(){
		var buttonGroup = $('.bwizard-buttons');
		$('#wizardForm').prepend(buttonGroup);
		buttonGroup.before('<legend />');
		$('.wizard-page').removeClass('well');
		$('div.wizard-page').css('min-height', 300);
		$(document).keyup(function(e) {
		    if(e.which == 27){
		    	$('div.modal').modal('hide');
		    }
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}, window.top));