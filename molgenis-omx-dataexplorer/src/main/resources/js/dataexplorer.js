(function($, w) {
	"use strict";

	var MAX_ROWS = 20;
	var ns = w.molgenis = w.molgenis || {};

	var resourceCache = {};

	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var currentPage = 1;
	var sortRule = null;

	// fill dataset select
	ns.fillDataSetSelect = function(callback) {
		ns.getAsync('/api/v1/dataset', function(datasets) {
			var items = [];
			// TODO deal with multiple entity pages
			$.each(datasets.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.name + '</option>');
			});
			$('#dataset-select').html(items.join(''));
			$('#dataset-select').change(function() {
				ns.onDataSetSelectionChange($(this).val());
			});
			callback();
		});
	};

	ns.get = function(resourceUri) {
		var cachedResource = resourceCache[resourceUri];
		if (cachedResource) {
			console.log('retrieved ' + resourceUri + ' from cache', cachedResource);
		} else {
			$.ajax({
				url : resourceUri,
				async : false,
				success : function(resource) {
					console.log('retrieved ' + resourceUri + ' from server', resource);
					resourceCache[resourceUri] = resource;
					cachedResource = resource;
				}
			});
		}
		return cachedResource;
	};

	ns.getAsync = function(resourceUri, callback) {
		var cachedResource = resourceCache[resourceUri];
		if (cachedResource) {
			console.log('retrieved ' + resourceUri + ' from cache', cachedResource);
			callback(cachedResource);
		} else {
			$.getJSON(resourceUri, function(resource) {
				console.log('retrieved ' + resourceUri + ' from server', resource);
				resourceCache[resourceUri] = resource;
				callback(resource);
			});
		}
	};

	ns.createFeatureSelection = function(protocol) {
		function createChildren(protocolUri, featureOpts, protocolOpts) {
			var protocol = ns.get(protocolUri + '?expand=features,subprotocols');

			var children = [];
			if (protocol.subprotocols) {
				// TODO deal with multiple entity pages
				$.each(protocol.subprotocols.items, function() {
					children.push($.extend({
						key : this.href,
						title : this.name,
						tooltip : this.description,
						isFolder : true,
						isLazy : protocolOpts.expand != true,
						children : protocolOpts.expand ? createChildren(this.href, featureOpts, protocolOpts) : null
					}, protocolOpts));
				});
			}
			if (protocol.features) {
				// TODO deal with multiple entity pages
				$.each(protocol.features.items, function() {
					children.push($.extend({
						key : this.href,
						title : this.name,
						tooltip : this.description,
						icon : "../../img/filter-bw.png",
					}, featureOpts));
				});
			}
			return children;
		}

		function expandNodeRec(node) {
			if (node.childList == undefined) {
				node.toggleExpand();
			} else {
				$.each(node.childList, function() {
					expandNodeRec(this);
				});
			}
		}

		function onNodeSelectionChange(selectedNodes) {
			var sortedNodes = selectedNodes.sort(function(node1, node2) {
				return node1.getLevel() - node2.getLevel();
			});
			var sortedFeatures = $.map(sortedNodes, function(node) {
				return node.data.isFolder ? null : node.data.key;
			});
			ns.onFeatureSelectionChange(sortedFeatures);
		}

		var container = $('#feature-selection');
		if (container.children('ul').length > 0) {
			container.dynatree('destroy');
		}
		container.empty();
		if (typeof protocol === 'undefined') {
			container.append("<p>No features available</p>");
			return;
		}

		// render tree and open first branch
		container.dynatree({
			checkbox : true,
			selectMode : 3,
			minExpandLevel : 2,
			debugLevel : 0,
			children : [ {
				key : protocol.href,
				title : protocol.name,
				icon : false,
				isFolder : true,
				isLazy : true,
				children : createChildren(protocol.href, {
					select : true
				}, {})
			} ],
			onLazyRead : function(node) {
				// workaround for dynatree lazy parent node select bug
				var opts = node.isSelected() ? {
					expand : true,
					select : true
				} : {};
				var children = createChildren(node.data.key, opts, opts);
				node.setLazyNodeStatus(DTNodeStatus_Ok);
				node.addChild(children);
			},
			onClick : function(node, event) {
				if ((node.getEventTargetType(event) === "title" || node.getEventTargetType(event) === "icon") && !node.data.isFolder)
					ns.openFeatureFilterDialog(node.data.key);
			},
			onSelect : function(select, node) {
				// workaround for dynatree lazy parent node select bug
				if (select)
					expandNodeRec(node);
				onNodeSelectionChange(this.getSelectedNodes());
			},
			onPostInit : function() {
				$("#feature-selection-container").accordion();
				onNodeSelectionChange(this.getSelectedNodes());
			}
		});
	};

	ns.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		featureFilters = {};
		$('#feature-filters p').remove();
		selectedFeatures = [];
		searchQuery = null;
		sortRule = null;
		currentPage = 1;
		$("#observationset-search").val("");

		ns.getAsync(dataSetUri + '?expand=protocolUsed', function(dataset) {
			selectedDataSet = dataset;
			ns.createFeatureSelection(dataset.protocolUsed);
		});
	};

	ns.onFeatureSelectionChange = function(featureUris) {
		selectedFeatures = featureUris;
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
		ns.search(function(searchResponse) {
			var maxRowsPerPage = MAX_ROWS;
			var nrRows = searchResponse.totalHitCount;

			var items = [];
			items.push('<thead>');
			$.each(selectedFeatures, function(i, val) {
				var feature = ns.get(this);
				if (sortRule && sortRule.value == feature.identifier) {
					if (sortRule.operator == 'SORTASC') {
						items.push('<th>' + feature.name + '<span data-value="' + feature.identifier
								+ '" class="ui-icon ui-icon-triangle-1-s down"></span></th>');
					} else {
						items.push('<th>' + feature.name + '<span data-value="' + feature.identifier
								+ '" class="ui-icon ui-icon-triangle-1-n up"></span></th>');
					}
				} else {
					items.push('<th>' + feature.name + '<span data-value="' + feature.identifier
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
					var feature = ns.get(this);
					var value = columnValueMap[feature.identifier];
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
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
				});
				break;
			case "date":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="date" autofocus="autofocus">');
				else
					filter = $('<input type="date" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.change(function() {
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
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
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
				});
				break;
			case "integer":
			case "int":
				var config = featureFilters[featureUri];
				
				var fromFilter;
				if (config == null)
					fromFilter = $('<input id="from" type="number" autofocus="autofocus" step="any">');
				else
					fromFilter = $('<input id="from" type="number" autofocus="autofocus" step="any" value="' + config.values[0] + '">');
				
				fromFilter.change(function() {
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $('#from').val(), $('#to').val()],
						range: true
					});
				});
				
				var toFilter;
				if (config == null)
					toFilter = $('<input id="to" type="number" autofocus="autofocus" step="any">');
				else
					toFilter = $('<input id="to" type="number" autofocus="autofocus" step="any" value="' + config.values[1] + '">');
				
				toFilter.change(function() {
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $('#from').val(), $('#to').val()],
						range: true
					});
				});
				
				filter = $('<span>From:<span>').after(fromFilter).after($('<span>To:</span>')).after(toFilter);
				break;
			case "decimal":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="number" autofocus="autofocus" step="any">');
				else
					filter = $('<input type="number" autofocus="autofocus" step="any" value="' + config.values[0] + '">');
				filter.change(function() {
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
				});
				break;
			case "bool":
				var config = featureFilters[featureUri];
				if (config == null)
					filter = $('<input type="checkbox" autofocus="autofocus">');
				else
					filter = $('<input type="checkbox" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.change(function() {
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
				});
				break;
			case "categorical":
				$.ajax({
					type : 'POST',
					url : '/api/v1/category?_method=GET',
					data : JSON.stringify({
						q : [ {
							"field" : "observableFeature_Identifier",
							"operator" : "EQUALS",
							"value" : feature.identifier
						} ]
					}),
					contentType : 'application/json',
					async : false,
					success : function(categories) {
						filter = [];
						$.each(categories.items, function() {
							var input = $('<input type="checkbox" name="' + feature.identifier + '" value="' + this.name + '">');

							input.change(function() {
								ns.updateFeatureFilter(featureUri, {
									name : feature.name,
									identifier : feature.identifier,
									type : feature.dataType,
									values : $.makeArray($('input[name="' + feature.identifier + '"]:checked').map(function() {
										return $(this).val();
									}))
								});
							});
							filter.push($('<label class="checkbox">').html(' ' + this.name).prepend(input));
						});
					}
				});
				break;
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

	ns.updateFeatureFilter = function(featureUri, featureFilter) {
		featureFilters[featureUri] = featureFilter;
		ns.onFeatureFilterChange(featureFilters);
	};

	ns.removeFeatureFilter = function(featureUri) {
		delete featureFilters[featureUri];
		ns.onFeatureFilterChange(featureFilters);
	};

	ns.onFeatureFilterChange = function(featureFilters) {
		ns.createFeatureFilterList(featureFilters);
		ns.updateObservationSetsTable();
	};

	ns.createFeatureFilterList = function(featureFilters) {
		var items = [];
		$.each(featureFilters, function(featureUri, feature) {
			items.push('<p><a class="feature-filter-edit" data-href="' + featureUri + '" href="#">' + feature.name + ' ('
					+ feature.values.join(',') + ')</a><a class="feature-filter-remove" data-href="' + featureUri
					+ '" href="#" title="Remove ' + feature.name + ' filter" ><i class="ui-icon ui-icon-closethick"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
		$('#feature-filters-container').accordion('destroy').accordion({
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
		
			$.each(filter.values, function(index, value) {
				if (index > 0) {
					searchRequest.queryRules.push({
						operator : 'OR'
					});
				}
				searchRequest.queryRules.push({
					field : filter.identifier,
					operator : 'EQUALS',
					value : value
				});
				
			});

			count++;
		});

		if (sortRule) {
			searchRequest.queryRules.push(sortRule);
		}

		searchRequest.fieldsToReturn = [];
		$.each(selectedFeatures, function() {
			var feature = ns.get(this);
			searchRequest.fieldsToReturn.push(feature.identifier);
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
			autoOpen : false
		});
	});
}($, window));