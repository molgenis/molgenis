(function($, molgenis, w) {
	"use strict";
	var searchApi = new molgenis.SearchClient();
	var pagination = new molgenis.Pagination();
	
	molgenis.createMatrixForDataItems = function(options){
		var documentType = 'protocolTree-' + options.dataSetId;
		var q = {
			'rules' : [[{
				'field' : 'type',
				'operator' : 'SEARCH',
				'value' : 'observablefeature'
			}]]
		};
		if(options.queryText !== ''){
			q.rules[0].push({
				'operator' : 'AND'
			});
			q.rules[0].push({
				'operator' : 'SEARCH',
				'value' : options.queryText
			});
			pagination.reset();
		}
		if(options.sortRule !== null)
		{
			q.sort = options.sortRule;
		}
		searchApi.search(createSearchRequest(documentType, q), function(searchResponse){
			var searchHits = searchResponse.searchHits;
			var table = $('<table />').addClass('table table-condensed');
			var body = $('<tbody />');
			$.each(searchHits, function(){
				$(options.createTableRow($(this)[0]['columnValueMap'])).appendTo(body);
			});
			table.append(createTableHeader(options)).append(body);
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
			pagination.updateMatrixPagination($('.pagination ul'), molgenis.createMatrixForDataItems, options);
			options.container.find('table').remove();
			options.container.prepend(table);
		});
		
		function createSearchRequest (documentType, q) {
			q.pageSize = pagination.getPager();
			q.offset = (pagination.getCurrentPage() - 1) * pagination.getPager();
			var searchRequest = {
				documentType : documentType,
				query: q
			};
			return searchRequest;
		}
		
		function createTableHeader(options){
			var headerRow = $('<tr />');
			if(options.tableHeaders.length > 0){
				var firstColumn = $('<th>' + options.tableHeaders[0] + '</th>').css('width', '30%').appendTo(headerRow);
				if(options.sortRule){
					if (options.sortRule.orders[0].direction == 'ASC') {
						$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-s down float-right"></span>').appendTo(firstColumn);
					} else {
						$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-n up float-right"></span>').appendTo(firstColumn);
					}
				} else {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-2-n-s updown float-right"></span>').appendTo(firstColumn);
				}
				
				$.each(options.tableHeaders, function(index, eachHeader){
					if(index > 0){
						$('<th>' + eachHeader + '</th>').appendTo(headerRow);
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
	};
}($, window.top.molgenis = window.top.molgenis || {}, window.top));