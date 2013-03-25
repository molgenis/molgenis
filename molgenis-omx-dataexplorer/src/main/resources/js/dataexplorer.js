(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};

	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;

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
								+ '\'"><label class="checkbox"><input type="checkbox" class="feature-select-checkbox" value="' + this.name
								+ '" checked>' + this.name + '</label><a class="feature-filter-edit" data-href="' + this.href
								+ '"href="#"><i class="icon-filter"></i></a></li>');
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

		var items = [];
		items.push('<thead>');
		$.each(selectedFeatures, function(i, val) {
			items.push('<th>' + this + '</th>');
		});
		items.push('</thead>');

		// simulate data
		var getRandomInt = function(min, max) {
			return Math.floor(Math.random() * (max - min + 1)) + min;
		};
		var maxRowsPerPage = 20;
		var nrRows = getRandomInt(1, 200);

		items.push('<tbody>');
		for ( var i = 0; i < Math.min(nrRows, maxRowsPerPage); ++i) {
			items.push('<tr>');
			$.each(selectedFeatures, function() {
				items.push('<td>' + this + '</td>');
			});
			items.push('</tr>');
		}
		items.push('</tbody>');
		$('#data-table').html(items.join(''));

		ns.onObservationSetsTableChange(nrRows, maxRowsPerPage);
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

	// on document ready
	$(function() {
		$("#observationset-search").focus();
		$("#observationset-search").change(function(e) {
			ns.searchObservationSets($(this).val());
		});
	});
}($, window));