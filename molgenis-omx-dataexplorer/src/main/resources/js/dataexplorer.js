(function($, w) {
	"use strict";

	var molgenis = w.molgenis = w.molgenis || {};

	molgenis.RestClient = function RestClient(cache) {
		this.cache = cache === false ? null : [];
	};

	molgenis.RestClient.prototype.get = function(resourceUri, expands) {
		var apiUri = this._toApiUri(resourceUri, expands);
		var cachedResource = this.cache && this.cache[apiUri];
		if (cachedResource) {
			console.log('retrieved ' + apiUri + ' from cache', cachedResource);
		} else {
			var _this = this;
			$.ajax({
				dataType : 'json',
				url : apiUri,
				async : false,
				success : function(resource) {
					console.log('retrieved ' + apiUri + ' from server', resource);
					_this._cachePut(resourceUri, resource, expands);
					cachedResource = resource;
				}
			});
		}
		return cachedResource;
	};

	molgenis.RestClient.prototype.getAsync = function(resourceUri, expands, callback) {
		var apiUri = this._toApiUri(resourceUri, expands);
		var cachedResource = this._cacheGet[apiUri];
		if (cachedResource) {
			console.log('retrieved ' + apiUri + ' from cache', cachedResource);
			callback(cachedResource);
		} else {
			var _this = this;
			$.ajax({
				dataType : 'json',
				url : apiUri,
				async : true,
				success : function(resource) {
					console.log('retrieved ' + apiUri + ' from server', resource);
					_this._cachePut(resourceUri, resource, expands);
					callback(resource);
				}
			});
		}
	};

	molgenis.RestClient.prototype._cacheGet = function(resourceUri) {
		return this.cache !== null ? this.cache[resourceUri] : null;
	};

	molgenis.RestClient.prototype._cachePut = function(resourceUri, resource, expands) {
		var apiUri = this._toApiUri(resourceUri, expands);
		this.cache[apiUri] = resource;
		if (resource.items) {
			for ( var i = 0; i < resource.items.length; i++) {
				var nestedResource = resource.items[i];
				this.cache[nestedResource.href] = nestedResource;
			}
		}
		if (expands) {
			this.cache[resourceUri] = resource;
			for ( var i = 0; i < expands.length; i++) {
				var expand = resource[expands[i]];
				this.cache[expand.href] = expand;
				if (expand.items) {
					for ( var j = 0; j < expand.items.length; j++) {
						var expandedResource = expand.items[j];
						this.cache[expandedResource.href] = expandedResource;
					}
				}
			}
		}
	};

	molgenis.RestClient.prototype._toApiUri = function(resourceUri, expands) {
		return expands ? resourceUri + '?expand=' + expands.join(',') : resourceUri;
	};
}($, window.top));

(function($, w) {
	"use strict";

	var molgenis = w.molgenis = w.molgenis || {};

	molgenis.SearchClient = function SearchClient() {
	};

	molgenis.SearchClient.prototype.search = function(searchRequest, callback) {
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
}($, window.top));

(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};
	
	var resultsTable = null;
	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var currentPage = 1;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();

	// fill dataset select
	ns.fillDataSetSelect = function(callback) {
		restApi.getAsync('/api/v1/dataset', null, function(datasets) {
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

	ns.createFeatureSelection = function(protocolUri) {
		function createChildren(protocolUri, featureOpts, protocolOpts) {
			var protocol = restApi.get(protocolUri, [ "features", "subprotocols" ]);

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
				return node1.getLevel() - node2.getLevel() <= 0 ? -1 : 1;
			});
			var sortedFeatures = $.map(sortedNodes, function(node) {
				return node.data.isFolder ? null : node.data.key;
			});
			ns.onFeatureSelectionChange(sortedFeatures);
		}

		restApi.getAsync(protocolUri, [ "features", "subprotocols" ], function(protocol) {
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
					$("#feature-selection-container").accordion({ collapsible: true });
					onNodeSelectionChange(this.getSelectedNodes());
				}
			});
		});
	};

	ns.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		featureFilters = {};
		$('#feature-filters p').remove();
		selectedFeatures = [];
		searchQuery = null;
		resultsTable.resetSortRule();
		currentPage = 1;
		$("#observationset-search").val("");

		restApi.getAsync(dataSetUri, null, function(dataSet) {
			selectedDataSet = dataSet;
			ns.createFeatureSelection(dataSet.protocolUsed.href);
		});
	};

	ns.onFeatureSelectionChange = function(featureUris) {
		selectedFeatures = featureUris;
		ns.updateObservationSetsTable();
	};

	ns.searchObservationSets = function(query) {
		console.log("searchObservationSets: " + query);

		// Reset
		resultsTable.resetSortRule();
		currentPage = 1;

		searchQuery = query;
		ns.updateObservationSetsTable();
	};

	ns.updateObservationSetsTable = function() {
		ns.search(function(searchResponse) {
			var maxRowsPerPage = resultsTable.getMaxRows();
			var nrRows = searchResponse.totalHitCount;
			
			resultsTable.build(searchResponse, selectedFeatures, restApi);
			
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
			$('#data-table-header').html(nrRows + ' data item found');
		else
			$('#data-table-header').html(nrRows + ' data items found');
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
		restApi.getAsync(featureUri, null, function(feature) {
			var items = [];
			if (feature.description)
				items.push('<h3>Description</h3><p>' + feature.description + '</p>');
			items.push('<h3>Value (' + feature.dataType + ')</h3>');
			var filter = null;
			var config = featureFilters[featureUri];
			
			switch (feature.dataType) {
			case "xref":
			case "string":
				if (config == null)
					filter = $('<input type="text" placeholder="filter text" autofocus="autofocus">');
				else
					filter = $('<input type="text" placeholder="filter text" autofocus="autofocus" value="' + config.values[0] + '">');
				filter.change(function() {
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
				});
				break;
			case "date":
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
			case "decimal":
				var fromFilter;
				if (config == null)
					fromFilter = $('<input id="from" type="number" autofocus="autofocus" step="any">');
				else
					fromFilter = $('<input id="from" type="number" autofocus="autofocus" step="any" value="' + config.values[0] + '">');

				fromFilter.change(function() {
					//If 'from' changed set 'to' at the same value
					var value = $('#from').val();
					$('#to').val(value);
					ns.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [value, value],
						range : true
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
						values : [ $('#from').val(), $('#to').val() ],
						range : true
					});
				});

				filter = $('<span>From:<span>').after(fromFilter).after($('<span>To:</span>')).after(toFilter);
				break;
			case "bool":
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
							var input;
							if (config && ($.inArray(this.name, config.values) > -1)) {
								input = $('<input type="checkbox" name="' + feature.identifier + '" value="' + this.name + '" checked>');
							} else {
								input = $('<input type="checkbox" name="' + feature.identifier + '" value="' + this.name + '">');
							}
			
							
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
			$('.feature-filter-dialog').dialog({ title: feature.name, dialogClass: 'ui-dialog-shadow' });
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
		searchApi.search(ns.createSearchRequest(), callback);
	};

	ns.createSearchRequest = function() {
		var searchRequest = {
			documentType : selectedDataSet.name,
			queryRules : [ {
				operator : 'LIMIT',
				value : resultsTable.getMaxRows()
			} ]
		};

		if (currentPage > 1) {
			var offset = (currentPage - 1) * resultsTable.getMaxRows();
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
				if (filter.range) {
					// Range filter
					var rangeAnd = false;
					if ((index == 0) && (value != '')) {
						searchRequest.queryRules.push({
							field : filter.identifier,
							operator : 'GREATER_EQUAL',
							value : value
						});
						rangeAnd = true;
					}
					if (rangeAnd) {
						searchRequest.queryRules.push({
							operator : 'AND'
						});
					}
					if ((index == 1) && (value != '')) {
						searchRequest.queryRules.push({
							field : filter.identifier,
							operator : 'LESS_EQUAL',
							value : value
						});
					}

				} else {
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
				}

			});

			count++;
		});

		var sortRule = resultsTable.getSortRule();
		if (sortRule) {
			searchRequest.queryRules.push(sortRule);
		}

		searchRequest.fieldsToReturn = [];
		$.each(selectedFeatures, function() {
			var feature = restApi.get(this);
			searchRequest.fieldsToReturn.push(feature.identifier);
		});

		return searchRequest;
	};

	// on document ready
	$(function() {
		resultsTable = new ns.ResultsTable();
		
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
}($, window.top));
