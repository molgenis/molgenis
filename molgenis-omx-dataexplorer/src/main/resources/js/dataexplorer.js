(function($, w) {
	"use strict";
	
	var MAX_ROWS = 10;
	var ns = w.molgenis = w.molgenis || {};

	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var offset = 0;
	
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
		ns.createFeatureSelectionRec(protocol.href, items);
		$('#feature-selection').html('<h3>Feature selection</h3><ul>' + items.join('') + '</ul>');

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
			var featureUri = $(this).parent().data('href');
			console.log("select feature: " + featureUri);
			ns.openEditFeatureFilterDialog(featureUri);
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
								+ '\'"><label class="checkbox"><input type="checkbox" class="feature-select-checkbox" value="' + this.identifier
								+ '" checked>' + this.name
								+ '</label><a class="feature-filter-edit" href="#"><i class="icon-filter"></i></a></li>');
					});
				}
				items.push('</ul></li>');
			}
		});

	};

	ns.onDataSetSelectionChange = function(dataSetUri) {
		console.log("onDataSetSelectionChange: " + dataSetUri);
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
			return $(this).val();
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
				items.push('<th>' + this + '</th>');
			});
			items.push('</thead>');

			items.push('<tbody>');
			for ( var i = 0; i < searchResponse.searchHits.length; ++i) {
				items.push('<tr>');
				var columnValueMap = searchResponse.searchHits[i].columnValueMap;
				
				$.each(selectedFeatures, function(i, val) {
					items.push('<td>' + columnValueMap[this] + '</td>');
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
		ns.updateObservationSetsTablePager(nrRows, maxRowsPerPage, 1);
		ns.updateObservationSetsTableHeader(nrRows);
	};

	ns.updateObservationSetsTableHeader = function(nrRows) {
		console.log("updateObservationSetsTableHeader");
		$('#data-table-header').html(nrRows + ' data items found');
	};

	ns.updateObservationSetsTablePager = function(nrRows, nrRowsPerPage, currentPage) {
		console.log("updateObservationSetsTablePager");
		$('#data-table-pager').empty();
		var nrPages = Math.ceil(nrRows / nrRowsPerPage);
		if (nrPages == 1)
			return;

		var items = [];
		items.push('<ul>');
		if (currentPage == 1)
			items.push('<li class="disabled"><a href="#">Prev</a></li>');
		else
			items.push('<li><a href="#">Prev</a></li>');
		for ( var i = 1; i <= Math.min(nrPages, 6); ++i) {
			if (i == currentPage)
				items.push('<li><a href="#">' + i + '</a></li>');
			else
				items.push('<li><a href="#" class="active">' + i + '</a></li>');
			if (nrPages >= 6 && i == 3) {
				items.push('<li class="disabled"><a href="#">...</a></li>');

			}
		}
		if (currentPage == nrPages)
			items.push('<li class="disabled"><a href="#">Next</a></li>');
		else
			items.push('<li><a href="#">Next</a></li>');
		items.push('</ul>');
		$('#data-table-pager').html(items.join(''));
	};

	ns.downloadExcel = function(filteredDataset) {
		console.log("downloadExcel: " + filteredDataset);
		// TODO need server controller to handle download
	};

	ns.openEditFeatureFilterDialog = function(featureUri) {
		console.log("openEditFeatureFilterDialog: " + featureUri);
		$.getJSON(featureUri, function(feature) {
			var items = [];
			$.each(feature, function(key, val) {
				items.push('<h3>' + key + '</h3><p>' + val + '</p>');
			});
			items.push('<button type="button" class="feature-filter-example">Mock filter update</button>');
			$('<div class="feature-filter-dialog">').html(items.join('')).dialog({
				modal : true,
				width : 800
			});
			$('.feature-filter-example').click(function() {
				ns.createFeatureFilter(feature, "<featureFilters>");
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
		$('#feature-filters-container').empty();
		$.each(featureFilters, function(featureUri, feature) {
			$('#feature-filters-container').append(
					'<p>' + feature.name + '<a class="feature-filter-remove" data-href="' + featureUri
							+ '" href="#"><i class="icon-remove"></i></a></p>');
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
			documentType: selectedDataSet.identifier,
			queryRules:[{operator:'LIMIT', value:MAX_ROWS}]
		};
		
		if (offset > 0) {
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
		$("#observationset-search").submit(function(e) {
			e.preventDefault();
			ns.searchObservationSets($('#observationset-search input').val());
		});
	});
}($, window));