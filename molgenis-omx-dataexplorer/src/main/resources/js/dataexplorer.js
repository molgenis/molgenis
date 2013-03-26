(function($, w) {
	"use strict";
	
	var MAX_ROWS = 20;
	var ns = w.molgenis = w.molgenis || {};

	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var currentPage = 1;
	
	// fill dataset select
	ns.fillDataSetSelect = function(callback) {
		console.log("getDataSets");
		$.getJSON('/api/v1/dataset', function(data) {
			console.log(data);
			var items = [];
			$.each(data.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.name + '</option>');
			});
			$('#dataset-select').html(items.join(''));
			$('#dataset-select').change(function() {
				ns.onDataSetSelectionChange($(this).val());
			});
			callback();
		});
	};

	// create protocol-feature tree
	ns.createFeatureSelection = function(protocol) {
		console.log("createFeatureSelection: " + protocol.href);
		// create feature list
		var items = [];
		items.push('<h3>Data item selection</h3>');
		items.push('<ul>');
		ns.createFeatureSelectionRec(protocol.href, items);
		items.push('</ul>');
		$('#feature-selection').html(items.join(''));
		$('#feature-selection').accordion('destroy').accordion({
			collapsible : true
		});

		// select feature
		$('.feature-select-checkbox').click(function() {
			ns.onFeatureSelectionChange();
		});
		// select protocol
		$('.protocol-select-checkbox').change(function() {
			var checked = typeof $(this).attr('checked') != 'undefined';
			$(this).parentsUntil('ul').find(':checkbox').not(this).attr('checked', checked);
			ns.onFeatureSelectionChange();
		});
		// filter feature
		$('.feature-filter-edit').click(function() {
			var featureUri = $(this).data('href');
			console.log("select feature: " + featureUri);
			ns.openFeatureFilterDialog(featureUri);
		});
	};

	// recursively create protocol-feature tree
	ns.createFeatureSelectionRec = function(protocolUri, items) {
		console.log("createFeatureSelectionRec: " + protocolUri);
		$.ajax({
			url : protocolUri + '?expand=features',
			dataType : 'json',
			async : false,
			success : function(protocol) {
				console.log("protocol: " + protocol.name);
				items.push('<li data="key: \'' + protocol.href + '\', title:\'' + protocol.name
						+ '\'"><label class="checkbox"><input type="checkbox" class="protocol-select-checkbox" value="' + protocol.name
						+ '" checked>' + protocol.name + '</label>');
				items.push('<ul>');
				if (protocol.subprotocols) {
					console.log("protocol.subprotocols.href: " + protocol.subprotocols.href);
					$.ajax({
						url : protocol.subprotocols.href,
						dataType : 'json',
						async : false,
						success : function(subprotocols) {
							console.log("subprotocols.href: " + subprotocols.href);
							if (subprotocols.items && subprotocols.items.length > 0) {
								$.each(subprotocols.items, function() {
									ns.createFeatureSelectionRec(this.href, items);
								});
							}
						}
					});
				}
				if (protocol.features) {
					$.each(protocol.features, function() {
						items.push('<li data-href="' + this.href + '" data="key: \'' + this.href + '\', title:\'' + this.name
								+ '\'"><label class="checkbox"><input type="checkbox" class="feature-select-checkbox" data-name="' + this.name 
								+ '" data-value="' + this.identifier + '" value="' + this.name 
								+ '" checked>' + this.name + '</label><a class="feature-filter-edit" data-href="' + this.href
								+ '" href="#"><i class="icon-filter"></i></a></li>');
					});
				}
				items.push('</ul></li>');
			}
		});

	};

	ns.onDataSetSelectionChange = function(dataSetUri) {
		console.log("onDataSetSelectionChange: " + dataSetUri);

		// reset
		featureFilters = {};
		selectedFeatures = [];
		searchQuery = null;

		$.ajax({
			url : dataSetUri + "?expand=protocolUsed",
			dataType : 'json',
			async : false,
			success : function(dataset) {
				selectedDataSet = dataset;
				ns.createFeatureSelection(dataset.protocolUsed);
				ns.onFeatureSelectionChange();
			}
		});
	};

	ns.onFeatureSelectionChange = function() {
		selectedFeatures = $('.feature-select-checkbox:checkbox:checked').sort(function(cb1, cb2) {
			return $(cb1).parents().length - $(cb2).parents().length;
		}).map(function() {
			return {identifier:$(this).attr('data-value'), name:$(this).attr('data-name')};
		}).get();
		ns.updateObservationSetsTable();
	};

	ns.searchObservationSets = function(query) {
		console.log("searchObservationSets: " + query);
		searchQuery = query;
		ns.updateObservationSetsTable();
	};

	ns.updateObservationSetsTable = function() {
		console.log("updateObservationSetsTable");

		console.log("query:            " + searchQuery);
		console.log("selectedFeatures: " + selectedFeatures);
		console.log("featureFilters:   " + featureFilters);
		
		ns.search(function(searchResponse){
			var maxRowsPerPage = MAX_ROWS;
			var nrRows = searchResponse.totalHitCount;
			
			var items = [];
			items.push('<thead>');
			$.each(selectedFeatures, function(i, val) {
				items.push('<th>' + this.name + '</th>');
			});
			items.push('</thead>');

			items.push('<tbody>');
			for ( var i = 0; i < searchResponse.searchHits.length; ++i) {
				items.push('<tr>');
				var columnValueMap = searchResponse.searchHits[i].columnValueMap;
				
				$.each(selectedFeatures, function(i, val) {
					items.push('<td>' + columnValueMap[this.identifier] + '</td>');
				});
				
				items.push('</tr>');
			}
			items.push('</tbody>');
			$('#data-table').html(items.join(''));
			
			ns.onObservationSetsTableChange(nrRows, maxRowsPerPage);
		});
	};

	ns.onObservationSetsTableChange = function(nrRows, maxRowsPerPage) {
		console.log("onObservationSetsTableChange");
		ns.updateObservationSetsTablePager(nrRows, maxRowsPerPage);
		ns.updateObservationSetsTableHeader(nrRows);
	};

	ns.updateObservationSetsTableHeader = function(nrRows) {
		console.log("updateObservationSetsTableHeader");
		$('#data-table-header').html(nrRows + ' data items found');
	};

	ns.updateObservationSetsTablePager = function(nrRows, nrRowsPerPage) {
		console.log("updateObservationSetsTablePager");
		$('#data-table-pager').empty();
		var nrPages = Math.ceil(nrRows / nrRowsPerPage);
		if (nrPages == 1)
			return;

		
		var pager = $('#data-table-pager');
		var ul = $('<ul>');
		pager.append(ul);
		
		if (currentPage == 1) {
			ul.append($('<li class="disabled"><span>&laquo;</span></li>'));
		} else {
			var prev = $('<li><a href="#">&laquo;</a></li>');
			prev.click(function(e){
				currentPage--;
				ns.updateObservationSetsTable();
				return false;
			});
			ul.append(prev);
		}
		
		for ( var i = 1; i <= nrPages; ++i) {
			if (i == currentPage) {
				ul.append($('<li class="active"><span>' + i + '</span></li>'));
				
			} else if ((i == 1) || (i == nrPages) || 
					   ((i > currentPage-3) && (i < currentPage+3)) || 
					   ((i < 7) && (currentPage < 5)) || 
					   ((i > nrPages-6) && (currentPage > nrPages-4))) {
				
				var p = $('<li><a href="#">' + i + '</a></li>');
				p.click((function(pageNr){
					return function(){
						currentPage = pageNr;
						ns.updateObservationSetsTable();
						return false;
					};
				})(i));
				
				ul.append(p);
			} else if ((i == 2) || (i == nrPages-1)) {
				ul.append($('<li class="disabled"><span>...</span></li>'));
				
			}
		}
		
		if (currentPage == nrPages) {
			ul.append($('<li class="disabled"><span>&raquo;</span></li>'));
		} else {
			var next = $('<li><a href="#">&raquo;</a></li>');
			next.click(function(){
				currentPage++;
				ns.updateObservationSetsTable();
				return false;
			});
			ul.append(next);
		}
	
		pager.append($('</ul>'));
	};

	ns.openFeatureFilterDialog = function(featureUri) {
		console.log("openFeatureFilterDialog: " + featureUri);
		$.getJSON(featureUri, function(feature) {
			var items = [];
			if (feature.description)
				items.push('<h3>Description</h3><p>' + feature.description + '</p>');
			items.push('<h3>Value (' + feature.dataType + ')</h3>');
			var filter = null;
			switch (feature.dataType) {
			case "string":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="text" placeholder="filter text">');
				else
					filter = $('<input type="text" placeholder="filter text" value="' + config.values[0] + '">');
				filter.keyup(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "date":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="date">');
				else
					filter = $('<input type="date" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				filter.datepicker();
				break;
			case "datetime":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="datetime">');
				else
					filter = $('<input type="datetime" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "int":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="number">');
				else
					filter = $('<input type="number" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "decimal":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="number" step="any">');
				else
					filter = $('<input type="number" step="any" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "bool":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="checkbox">');
				else
					filter = $('<input type="checkbox" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "categorical":
			case "xref":
			case "nominal":
			case "ordinal":
			case "code":
			case "image":
			case "file":
			case "log":
			case "data":
			case "exe":
				console.log("TODO: '" + feature.dataType + "' not supported");
				break;
			}
			$('<div class="feature-filter-dialog">').html(items.join('')).append(filter).dialog({
				title : feature.name,
				modal : true,
				width : 500
			});
		});
	};

	ns.createFeatureFilter = function(feature, featureFilter) {
		console.log("createFeatureFilter: " + feature.href);
		featureFilters[feature.href] = feature;
		ns.onFeatureFilterChange();
	};

	ns.removeFeatureFilter = function(featureUri) {
		console.log("removeFeatureFilter: " + featureUri);
		delete featureFilters[featureUri];
		ns.onFeatureFilterChange();
	};

	ns.onFeatureFilterChange = function() {
		console.log("onFeatureFilterChange");
		var items = [];
		items.push('<h3>Data item filters</h3><div>');
		$.each(featureFilters, function(featureUri, feature) {
			items.push('<p><a class="feature-filter-edit" data-href="' + featureUri + '" href="#">' + feature.name
					+ '</a><a class="feature-filter-remove" data-href="' + featureUri + '" href="#"><i class="icon-remove"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
		$('#feature-filters').accordion('destroy').accordion({
			collapsible : true
		});

		$('.feature-filter-edit').click(function() {
			ns.openFeatureFilterDialog($(this).data('href'));
		});
		$('.feature-filter-remove').click(function() {
			ns.removeFeatureFilter($(this).data('href'));
		});

		ns.updateObservationSetsTable();
	};

	ns.search = function(callback) {
		ns.callSearchService(ns.createSearchRequest(), callback);
	};
	
	ns.createSearchRequest = function() {
		var searchRequest = {
			documentType: selectedDataSet.name,
			queryRules:[{operator:'LIMIT', value:MAX_ROWS}]
		};
		
		if (currentPage > 1) {
			var offset = (currentPage - 1) * MAX_ROWS;
			searchRequest.queryRules.push({operator:'OFFSET', value:offset});
		}
		
		if (searchQuery) {
			searchRequest.queryRules.push({operator:'SEARCH', value:searchQuery});
		}
		
		return searchRequest;
	};
	
	ns.callSearchService = function(searchRequest, callback) {
		var jsonRequest = JSON.stringify(searchRequest);
		console.log("Call SearchService json=" + jsonRequest);
		
		$.ajax({
			type: "POST",
			url: '/search',
			data: jsonRequest,
			contentType: 'application/json',
			success: function (searchResponse) {
				if (searchResponse.errorMessage) {
					alert(searchResponse.errorMessage);
				}
				callback(searchResponse);
			}
		});

	};
	
	// on document ready
	$(function() {
		$("#observationset-search").focus();
		$("#observationset-search").change(function(e) {
			ns.searchObservationSets($(this).val());
		});
	});
}($, window));