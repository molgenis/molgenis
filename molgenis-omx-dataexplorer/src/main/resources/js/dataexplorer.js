(function($, w) {
	"use strict";

	var MAX_ROWS = 20;
	var ns = w.molgenis = w.molgenis || {};

	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var currentPage = 1;
	var sortRule = null;

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
			return false;
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
								+ '\'"><label class="checkbox"><input type="checkbox" class="feature-select-checkbox" data-name="'
								+ this.name + '" data-value="' + this.identifier + '" value="' + this.name + '" checked>' + this.name
								+ '</label><a class="feature-filter-edit" data-href="' + this.href
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
		$('#feature-filters p').remove();
		selectedFeatures = [];
		searchQuery = null;
		sortRule = null;
		currentPage = 1;
		$("#observationset-search").val("");

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
			return {
				identifier : $(this).attr('data-value'),
				name : $(this).attr('data-name')
			};
		}).get();
		ns.updateObservationSetsTable();
	};

	ns.searchObservationSets = function(query) {
		console.log("searchObservationSets: " + query);

		// Reset
		featureFilters = {};
		$('#feature-filters p').remove();
		sortRule = null;
		currentPage = 1;

		searchQuery = query;
		ns.updateObservationSetsTable();
	};

	ns.updateObservationSetsTable = function() {
		console.log("updateObservationSetsTable");

		console.log("query:            " + searchQuery);
		console.log("selectedFeatures: " + selectedFeatures);
		console.log("featureFilters:   " + featureFilters);

		ns.search(function(searchResponse) {
			var maxRowsPerPage = MAX_ROWS;
			var nrRows = searchResponse.totalHitCount;

			var items = [];
			items.push('<thead>');
			$.each(selectedFeatures, function(i, val) {
				if (sortRule && sortRule.value == this.identifier) {
					if (sortRule.operator == 'SORTASC') {
						items.push('<th>' + this.name + '<span data-value="' + this.identifier
								+ '" class="ui-icon ui-icon-triangle-1-s down"></span></th>');
					} else {
						items.push('<th>' + this.name + '<span data-value="' + this.identifier
								+ '" class="ui-icon ui-icon-triangle-1-n up"></span></th>');
					}
				} else {
					items.push('<th>' + this.name + '<span data-value="' + this.identifier
							+ '" class="ui-icon ui-icon-triangle-2-n-s updown"></span></th>');
				}
			});
			items.push('</thead>');

			items.push('<tbody>');
			
			if (nrRows == 0) {
				items.push('<tr><td class="nothing-found" colspan="' + selectedFeatures.length + '">Nothing found</td></tr>');
			}
			
			for ( var i = 0; i < searchResponse.searchHits.length; ++i) {
				items.push('<tr>');
				var columnValueMap = searchResponse.searchHits[i].columnValueMap;

				$.each(selectedFeatures, function(i, val) {
					var value = columnValueMap[this.identifier];
					if (value) {
						items.push('<td>' + value + '</td>');
					} else {
						items.push('<td></td>');
					}
				});

				items.push('</tr>');
			}
			items.push('</tbody>');
			$('#data-table').html(items.join(''));

			// Sort click
			$('#data-table thead th .ui-icon').click(function() {
				if (nrRows == 0) {
					return;
				}
				
				var featureIdentifier = $(this).data('value');
				console.log("select sort column: " + featureIdentifier);
				if (sortRule && sortRule.operator == 'SORTASC') {
					sortRule = {
						value : featureIdentifier,
						operator : 'SORTDESC'
					};
				} else {
					sortRule = {
						value : featureIdentifier,
						operator : 'SORTASC'
					};
				}
				ns.updateObservationSetsTable();
				return false;
			});

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
		if (nrRows == 1)
			$('#data-table-header').html(nrRows + ' data items found');
		else
			$('#data-table-header').html(nrRows + ' data item found');
	};

	ns.updateObservationSetsTablePager = function(nrRows, nrRowsPerPage) {
		console.log("updateObservationSetsTablePager");
		$('#data-table-pager').empty();
		var nrPages = Math.ceil(nrRows / nrRowsPerPage);
		if (nrPages <= 1)
			return;

		var pager = $('#data-table-pager');
		var ul = $('<ul>');
		pager.append(ul);

		if (currentPage == 1) {
			ul.append($('<li class="disabled"><span>&laquo;</span></li>'));
		} else {
			var prev = $('<li><a href="#">&laquo;</a></li>');
			prev.click(function(e) {
				currentPage--;
				ns.updateObservationSetsTable();
				return false;
			});
			ul.append(prev);
		}

		for ( var i = 1; i <= nrPages; ++i) {
			if (i == currentPage) {
				ul.append($('<li class="active"><span>' + i + '</span></li>'));

			} else if ((i == 1) || (i == nrPages) || ((i > currentPage - 3) && (i < currentPage + 3)) || ((i < 7) && (currentPage < 5))
					|| ((i > nrPages - 6) && (currentPage > nrPages - 4))) {

				var p = $('<li><a href="#">' + i + '</a></li>');
				p.click((function(pageNr) {
					return function() {
						currentPage = pageNr;
						ns.updateObservationSetsTable();
						return false;
					};
				})(i));

				ul.append(p);
			} else if ((i == 2) || (i == nrPages - 1)) {
				ul.append($('<li class="disabled"><span>...</span></li>'));

			}
		}

		if (currentPage == nrPages) {
			ul.append($('<li class="disabled"><span>&raquo;</span></li>'));
		} else {
			var next = $('<li><a href="#">&raquo;</a></li>');
			next.click(function() {
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
					filter = $('<input type="text" placeholder="filter text" autofocus="autofocus">');
				else
					filter = $('<input type="text" placeholder="filter text" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.keyup(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "date":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="date" autofocus="autofocus">');
				else
					filter = $('<input type="date" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						identifier : feature.identifier,
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
					filter = $('<input type="datetime" autofocus="autofocus">');
				else
					filter = $('<input type="datetime" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "integer":
			case "int":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="number" autofocus="autofocus" step="any">');
				else
					filter = $('<input type="number" autofocus="autofocus" step="any" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "decimal":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="number" autofocus="autofocus" step="any">');
				else
					filter = $('<input type="number" autofocus="autofocus" step="any" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					};
					ns.onFeatureFilterChange();
				});
				break;
			case "bool":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="checkbox" autofocus="autofocus">');
				else
					filter = $('<input type="checkbox" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.change(function() {
					featureFilters[featureUri] = {
						name : feature.name,
						identifier : feature.identifier,
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
			$('.feature-filter-dialog').html(items.join('')).append(filter);
			$('.feature-filter-dialog').dialog('option', 'title', feature.name);
			$('.feature-filter-dialog').dialog('open');
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
					+ '</a><a class="feature-filter-remove" data-href="' + featureUri + '" href="#" title="Remove ' + feature.name + ' filter" ><i class="ui-icon ui-icon-closethick"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
		$('#feature-filters').accordion('destroy').accordion({
			collapsible : true
		});

		$('.feature-filter-edit').click(function() {
			ns.openFeatureFilterDialog($(this).data('href'));
			return false;
		});
		$('.feature-filter-remove').click(function() {
			ns.removeFeatureFilter($(this).data('href'));
			return false;
		});

		ns.updateObservationSetsTable();
	};

	ns.search = function(callback) {
		ns.callSearchService(ns.createSearchRequest(), callback);
	};

	ns.createSearchRequest = function() {
		var searchRequest = {
			documentType : selectedDataSet.name,
			queryRules : [ {
				operator : 'LIMIT',
				value : MAX_ROWS
			} ]
		};

		if (currentPage > 1) {
			var offset = (currentPage - 1) * MAX_ROWS;
			searchRequest.queryRules.push({
				operator : 'OFFSET',
				value : offset
			});
		}

		var count = 0;

		if (searchQuery) {
			searchRequest.queryRules.push({
				operator : 'SEARCH',
				value : searchQuery
			});
			count++;
		}

		$.each(featureFilters, function(featureUri, filter) {
			if (count > 0) {
				searchRequest.queryRules.push({
					operator : 'AND'
				});
			}
			searchRequest.queryRules.push({
				field : filter.identifier,
				operator : 'EQUALS',
				value : filter.values[0]
			});
			count++;
		});

		if (sortRule) {
			searchRequest.queryRules.push(sortRule);
		}

		searchRequest.fieldsToReturn = [];
		$.each(selectedFeatures, function() {
			searchRequest.fieldsToReturn.push(this.identifier);
		});
		
		return searchRequest;
	};

	ns.callSearchService = function(searchRequest, callback) {
		var jsonRequest = JSON.stringify(searchRequest);
		console.log("Call SearchService json=" + jsonRequest);

		$.ajax({
			type : "POST",
			url : '/search',
			data : jsonRequest,
			contentType : 'application/json',
			success : function(searchResponse) {
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
		
		$('.feature-filter-dialog').dialog({
			modal : true,
			width : 500,
			autoOpen: false
		});
	});
}($, window));