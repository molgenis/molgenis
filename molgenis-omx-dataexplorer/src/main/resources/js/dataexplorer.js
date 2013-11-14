(function($, molgenis) {
	"use strict";
	
	var resultsTable = null;
	var pager = null;
	var featureFilters = {};
	var selectedFeatures = [];
	var searchQuery = null;
	var selectedDataSet = null;
	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();
	var aggregateView = false;
	
	molgenis.createFeatureSelection = function(protocolUri) {
		function createChildren(protocolUri, featureOpts, protocolOpts) {
			var subprotocols = restApi.get(protocolUri + '/subprotocols?num=500');
			
			var children = [];
			if (subprotocols.items) {
				$.each(subprotocols.items, function() {
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
			
			var featureNodes = createFeatureNodes(protocolUri + '/features', featureOpts);
			$.each(featureNodes, function() {
				children.push(this);
			});
			
			return children;
		}
			
		function createFeatureNodes(protocolFeaturesUri, featureOpts) {
			var features = restApi.get(protocolFeaturesUri);
			var nodes = [];
			
			if (features.items) {
				
				$.each(features.items, function() {
					nodes.push($.extend({
						key : this.href,
						title : this.name,
						tooltip : this.description,
						icon : "../../img/filter-bw.png",
					}, featureOpts));
				});
				
				if (features.nextHref) {
					nodes.push($.extend({
						key : 'more',
						nextHref: features.nextHref,
						title : '<i>more...</i>',
						icon: false,
						hideCheckbox: true,
						tooltip : 'Load more'
					}));
				}
			}

			return nodes;
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
			function getSiblingPos(node) {
				var pos = 0;
				do {
					node = node.getPrevSibling();
					if (node == null)
						break;
					else
						++pos;
				} while (true);
				return pos;
			}
			var sortedNodes = selectedNodes.sort(function(node1, node2) {
				var diff = node1.getLevel() - node2.getLevel();
				if (diff == 0) {
					diff = getSiblingPos(node1.getParent()) - getSiblingPos(node2.getParent());
					if (diff == 0)
						diff = getSiblingPos(node1) - getSiblingPos(node2);
				}
				return diff <= 0 ? -1 : 1;
			});
			var sortedFeatures = $.map(sortedNodes, function(node) {
				return node.data.isFolder || node.data.key == 'more' ? null : node.data.key;
			});
			molgenis.onFeatureSelectionChange(sortedFeatures);
		}

		restApi.getAsync(protocolUri, null, null, function(protocol) {
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
				checkbox : !aggregateView,
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
					
					if (node.data.key == 'more') {
						var nextFeatureNodes = createFeatureNodes(node.data.nextHref, {});
						node.remove();
						node.parent.addChild(nextFeatureNodes);
					}
					// target type null is filter icon
					if ((node.getEventTargetType(event) === "title" || node.getEventTargetType(event) === "icon" || node.getEventTargetType(event) === null) && !node.data.isFolder)
						molgenis.openFeatureFilterDialog(node.data.key);
				},
				onSelect : function(select, node) {
					// workaround for dynatree lazy parent node select bug
					if (select)
						expandNodeRec(node);
					onNodeSelectionChange(this.getSelectedNodes());
				},
				onPostInit : function() {
					onNodeSelectionChange(this.getSelectedNodes());
				}
			});
		});
	};

	molgenis.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		featureFilters = {};
		$('#feature-filters p').remove();
		selectedFeatures = [];
		searchQuery = null;
		resultsTable.resetSortRule();
		$("#observationset-search").val("");
		$('#data-table-pager').empty();
		pager = null;
		
		restApi.getAsync(dataSetUri, null, null, function(dataSet) {
			selectedDataSet = dataSet;
			molgenis.createFeatureSelection(dataSet.protocolUsed.href);
		});
	};

	molgenis.onFeatureSelectionChange = function(featureUris) {
		selectedFeatures = featureUris;
		molgenis.updateObservationSetsTable();
	};

	molgenis.searchObservationSets = function(query) {
		// Reset
		resultsTable.resetSortRule();

		searchQuery = query;
		molgenis.updateObservationSetsTable();
	};
	
	molgenis.updateObservationSetsTable = function() {
		if (selectedFeatures.length > 0) {
			molgenis.search(function(searchResponse) {
				var maxRowsPerPage = resultsTable.getMaxRows();
				var nrRows = searchResponse.totalHitCount;
				
				resultsTable.build(searchResponse, selectedFeatures, restApi);

				molgenis.onObservationSetsTableChange(nrRows, maxRowsPerPage);
			});
		} else {
			$('#nrOfDataItems').html('no features selected');
			$('#data-table-pager').empty();
			$('#data-table').empty();
		}
	};

	molgenis.updateObservationSetsTablePage = function() {
		if (selectedFeatures.length > 0) {
			molgenis.search(function(searchResponse) {
				resultsTable.build(searchResponse, selectedFeatures, restApi);
			});
		} else {
			$('#nrOfDataItems').html('no features selected');
			$('#data-table-pager').empty();
			$('#data-table').empty();
		}
	};
	
	molgenis.onObservationSetsTableChange = function(nrRows, maxRowsPerPage) {
		molgenis.updateObservationSetsTablePager(nrRows, maxRowsPerPage);
		molgenis.updateObservationSetsTableHeader(nrRows);
	};

	molgenis.updateObservationSetsTableHeader = function(nrRows) {
		if (nrRows == 1)
			$('#nrOfDataItems').html(nrRows + ' data item found');
		else
			$('#nrOfDataItems').html(nrRows + ' data items found');
	};

	molgenis.updateObservationSetsTablePager = function(nrRows, nrRowsPerPage) {
		$('#data-table-pager').pager({
			'nrItems': nrRows,
			'nrItemsPerPage': nrRowsPerPage,
			'onPageChange': molgenis.updateObservationSetsTablePage
		});
		pager = $('#data-table-pager');
	};

	molgenis.openFeatureFilterDialog = function(featureUri) {
		restApi.getAsync(featureUri, null, null, function(feature) {
		
			var items = [];
			if (feature.description)
				items.push('<h3>Description</h3><p>' + feature.description + '</p>');
			items.push('<h3>Filter:</h3>');
			var filter = null;
			var config = featureFilters[featureUri];
			var applyButton = $('<input type="button" class="btn pull-left" value="Apply filter">');
			
			var divContainer = molgenis.createFeatureField(items, feature, config, applyButton,featureUri);
			molgenis.specificOneFeature(items, divContainer, feature, config, applyButton,featureUri);
		});
	
	};

	//Filter dialog for one feature
	molgenis.specificOneFeature = function(items,divContainer,feature,config,applyButton,featureUri) {
		switch (feature.dataType) {
			case "html":
			case "mref":
			case "xref":
			case "email":
			case "hyperlink":
			case "text":
			case "string":
				divContainer.keyup(function(e){
					if (config == null) {
						applyButton.attr('disabled', 'disabled');
					}
					if (divContainer.val() == '') {
						applyButton.attr('disabled', 'disabled');
					} else {
						applyButton.removeAttr('disabled');
					}
					applyButton.click(function() {
						molgenis.updateFeatureFilter(featureUri, {
							name : feature.name,
							identifier : feature.identifier,
							type : feature.dataType,
							values : [ divContainer.val() ]
						});
						$('.feature-filter-dialog').dialog('close');
					});
				});
			break;
			case "date":
			case "datetime":
				console.log($(divContainer));
				if (config == null) {
					applyButton.attr('disabled', 'disabled');
				}
				divContainer.find('#from_'+feature.identifier + ':eq(0)').on('changeDate', function(e) {
					applyButton.removeAttr('disabled');
				});
				$( ".feature-filter-dialog" ).dialog( "option", "width", 710 );
				divContainer.find('#to_'+feature.identifier + ':eq(0)').on('changeDate', function(e){
					applyButton.removeAttr('disabled');
				});
				
				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						range: true,
						values : [ $('#date-feature-from_'+feature.identifier+'').val().replace("'T'", "T"), $('#date-feature-to_'+feature.identifier+'').val().replace("'T'", "T")]
					});
					$('.feature-filter-dialog').dialog('close');
				});
			break;
			break;
			case "long":
			case "integer":
			case "int":
			case "decimal":
				if (config == null) {
					applyButton.attr('disabled', 'disabled');
				}
				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $('#from').val(), $('#to').val() ],
						range : true
					});
					$('.feature-filter-dialog').dialog('close');
				});
				$('input[type=number]').live('keyup input', function(e) {
					if ((fromFilter.val() == '') && (toFilter.val() == '')) {
						applyButton.attr('disabled', 'disabled');
					} else {
						applyButton.removeAttr('disabled');
					}
				});
			break;
			case "bool":
				if (config == null) {
					applyButton.attr('disabled', 'disabled');
				}
				$('input[name=bool-feature]').live('change', function() {
					applyButton.removeAttr('disabled');
				});
	
				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $('input[name=bool-feature]:checked').val() ]
					});
					$('.feature-filter-dialog').dialog('close');
				});
			break;
			case "categorical":
				if (config && config.values.length > 0) {
					applyButton.removeAttr('disabled');
				} else {
					applyButton.attr('disabled', 'disabled');
				}
				$('.cat-value').live('change', function() {
					if ($('.cat-value:checked').length > 0) {
						applyButton.removeAttr('disabled');
					} else {
						applyButton.attr('disabled', 'disabled');
					}
				});
				
				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : $.makeArray($('.cat-value:checked').map(function() {
							return $(this).val();
						}))
					});
					$('.feature-filter-dialog').dialog('close');
				});
			break;
			default:
				console.log("TODO: '" + feature.dataType + "' not supported");
				return;
			
		}
		var applyButtonHolder = $('<div id="applybutton-holder" />').append(applyButton);
		if ($.isArray(divContainer)) {
			divContainer.push(applyButtonHolder);
		} else {
			divContainer.after(applyButtonHolder);
		}
		$('.feature-filter-dialog').html(items.join('')).append(divContainer);
		
		
		$('.feature-filter-dialog').dialog({
			title : feature.name,
			dialogClass : 'ui-dialog-shadow'
		});
		$('.feature-filter-dialog').dialog('open');
		
		$('.feature-filter-dialog').keyup(function(e) {
			  if (e.keyCode == 13) // enter
			  {
				  if(applyButton.attr("disabled")!="disabled"){//enter only works if button is enabled (filter input is given)
					  	applyButton.click(); 
				  }
			  }     
			  if (e.keyCode == 27)// esc
			  { 
				  $('.feature-filter-dialog').dialog('close'); 
			  }   
		});
	};
	//Generic part for filter fields
	molgenis.createFeatureField = function (items,feature,config,applyButton,featureUri){
		var divContainer = $('<div />');
		var filter = null;
		
		switch (feature.dataType) {
			case "html":
			case "mref":
			case "xref":
			case "email":
			case "hyperlink":
			case "text":
			case "string":
				
				if (config == null) 				
					filter = $('<input type="text" id="text_'+feature.identifier+'" placeholder="filter text" autofocus="autofocus">');
					console.log(feature.identifier);
				} else {

					filter = $('<input type="text" placeholder="filter text" autofocus="autofocus" value="' + config.values[0] + '">');
				}
				$('#text_'+feature.identifier).change(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : feature.name,
						identifier : feature.identifier,
						type : feature.dataType,
						values : [ $(this).val() ]
					});
				
				});
				
				break;
			case "date":
			case "datetime":	
				var datePickerFrom = $('<div id="from_'+feature.identifier+'" class="input-append date" />');
				var filterFrom;
				
				if (config == null) {
					filterFrom = datePickerFrom.append($('<input id="date-feature-from_'+feature.identifier+'"  type="text" /><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				} else {
					filterFrom = datePickerFrom.append($('<input id="date-feature-from_'+feature.identifier+'"  type="text" value="' + config.values[0].replace("T", "'T'") + '" /><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				}
				
				datePickerFrom.on('changeDate', function(e) {
					$('#date-feature-to_'+feature.identifier+'').val($('#date-feature-from_'+feature.identifier+'').val());
				});

				var datePickerTo = $('<div id="to_'+feature.identifier+'" class="input-append date" />');
				var filterTo;
				
				if (config == null)
					filterTo = datePickerTo.append($('<input id="date-feature-to_'+feature.identifier+'" type="text"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				else
					filterTo = datePickerTo.append($('<input id="date-feature-to_'+feature.identifier+'" type="text" value="' + config.values[1].replace("T", "'T'") + '"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				
				filter = $('<span>From:<span>').after(filterFrom).after($('<span>To:</span>')).after(filterTo);
				
				var dateArray = [];
				dateArray.push(datePickerFrom.find('#date-feature-from_'+feature.identifier));
				dateArray.push(datePickerTo.find('#date-feature-to_'+feature.identifier));
				
				$.each(dateArray, function(index, datePickerElement){
					datePickerElement.change(function() {
						molgenis.updateFeatureFilter(featureUri, {
							name : feature.name,
							identifier : feature.identifier,
							type : feature.dataType,
							range: true,
							values : [ $('#date-feature-from_'+feature.identifier+'').val().replace("'T'", "T"), $('#date-feature-to_'+feature.identifier+'').val().replace("'T'", "T")]
						});
						$('.feature-filter-dialog').dialog('close');
					});
				});
				
				break;
			case "long":
			case "integer":
			case "int":
			case "decimal":
				var fromFilter;
				var toFilter;
				
				if (config == null) {
					fromFilter = $('<input id="from_'+feature.identifier+'" type="number" autofocus="autofocus" step="any">');
					toFilter = $('<input id="to_'+feature.identifier+'" type="number" autofocus="autofocus" step="any">');	
				} else {
					fromFilter = $('<input id="from_'+feature.identifier+'" type="number" autofocus="autofocus" step="any" value="' + config.values[0] + '">');
					toFilter = $('<input id="to_'+feature.identifier+'" type="number" autofocus="autofocus" step="any" value="' + config.values[1] + '">');	
				}
				
				fromFilter.on('keyup input', function() {
					// If 'from' changed set 'to' at the same value
					$('#to_'+feature.identifier+'').val($('#from_'+feature.identifier+'').val());
				});
	
				filter = $('<span>From:<span>').after(fromFilter).after($('<span>To:</span>')).after(toFilter);

				break;
			case "bool":
				if (config == null) {
					filter = $('<label class="radio"><input type="radio" id="bool-feature-true_'+feature.identifier+'" name="bool-feature_'+feature.identifier+'" value="true">True</label><label class="radio"><input type="radio" id="bool-feature-fl_'+feature.identifier+'" name="bool-feature_'+feature.identifier+'" value="false">False</label>');

				} else {
					if (config.values[0] == 'true') {
						filter = $('<label class="radio"><input type="radio" id="bool-feature-true_'+feature.identifier+'" name="bool-feature_'+feature.identifier+'" checked value="true">True</label><label class="radio"><input type="radio" id="bool-feature-fl_'+feature.identifier+'" name="bool-feature_'+feature.identifier+'" value="false">False</label>');
					} else {
						filter = $('<label class="radio"><input type="radio" id="bool-feature-true_'+feature.identifier+'" name="bool-feature_'+feature.identifier+'" value="true">True</label><label class="radio"><input type="radio" id="bool-feature-fl_'+feature.identifier+'" name="bool-feature_'+feature.identifier+'" checked value="false">False</label>');
					}
				}
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
								input = $('<input type="checkbox" id="'+this.name +'_'+ feature.identifier + '" "class="cat-value" name="' + feature.identifier + '" value="' + this.name + '"checked>');
							} else {
								input = $('<input type="checkbox" id="'+this.name +'_'+ feature.identifier + '" class="cat-value" name="' + feature.identifier + '" value="' + this.name + '">');
							}
							filter.push($('<label class="checkbox">').html(' ' + this.name).prepend(input));
						});
					}
				});
				break;
			default:
				console.log("TODO: '" + feature.dataType + "' not supported");
				return;
			}
		
			divContainer.append(filter);

			if ((feature.dataType == 'xref') || (feature.dataType == 'mref')) {
				$('.feature-filter-dialog input[type=text]').autocomplete({
					source: function( request, response ) {
						$.ajax({
							type : 'POST',
							url : '/api/v1/characteristic?_method=GET',
							data : JSON.stringify({
								num : 15,
								q : [ {
									"field" : "name",
									"operator" : "LIKE",
									"value" : request.term
								} ]
							}),
							contentType : 'application/json',
							async : true,
							success : function(characteristicList) {
								response( $.map(characteristicList.items,function(item) {
									return item.name;
								}));
							}
						});
					},
					minLength: 2
				});
			}
			if (feature.dataType == 'date')  {
				divContainer.find('.date').datetimepicker({
					format: 'yyyy-MM-dd',
					language: 'en',
				    pickTime: false
				});
			} else if (feature.dataType == 'datetime') {
				divContainer.find('.date').datetimepicker({
					format: "yyyy-MM-dd'T'hh:mm:ss" + getCurrentTimezoneOffset(),
					language: 'en',
				    pickTime: true
				});
			}
		return divContainer;
	};
	
	molgenis.createFeatureFilterField = function(element, featureUri){
		var feature = restApi.get(featureUri, null, null);
			var items = [];
			var filter = null;
			var config = featureFilters[featureUri];
			var applyButton = $('<input type="button" class="btn pull-left" value="Apply filter">');
			var divContainer = molgenis.createFeatureField(items, feature, config, applyButton,featureUri);
			var trElement = $(element).parent().parent();
			trElement.append(divContainer);			
	};
	
	molgenis.updateFeatureFilter = function(featureUri, featureFilter) {
		featureFilters[featureUri] = featureFilter;
		molgenis.onFeatureFilterChange(featureFilters);
	};

	molgenis.removeFeatureFilter = function(featureUri) {
		delete featureFilters[featureUri];
		molgenis.onFeatureFilterChange(featureFilters);
	};

	molgenis.onFeatureFilterChange = function(featureFilters) {
		molgenis.createFeatureFilterList(featureFilters);
		molgenis.updateObservationSetsTable();
		if($('#selectFeature').val()!=null){
			molgenis.loadAggregate($('#selectFeature').val());
		}
	};

	molgenis.createFeatureFilterList = function(featureFilters) {
		var items = [];
		$.each(featureFilters, function(featureUri, feature) {
			items.push('<p><a class="feature-filter-edit" data-href="' + featureUri + '" href="#">' + feature.name + ' ('
					+ feature.values.join(',') + ')</a><a class="feature-filter-remove" data-href="' + featureUri
					+ '" href="#" title="Remove ' + feature.name + ' filter" ><i class="ui-icon ui-icon-closethick"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));

		$('.feature-filter-edit').click(function() {
			molgenis.openFeatureFilterDialog($(this).data('href'));
			return false;
		});
		$('.feature-filter-remove').click(function() {
			molgenis.removeFeatureFilter($(this).data('href'));
			return false;
		});
	};

	molgenis.search = function(callback) {
		searchApi.search(molgenis.createSearchRequest(true), callback);
	};

	molgenis.createSearchRequest = function(includeLimitOffset) {
		var searchRequest = {
			documentType : selectedDataSet.identifier,
			queryRules : [ ]
		};
		
		if (includeLimitOffset) {
			searchRequest.queryRules.push({
				operator : 'LIMIT',
				value : resultsTable.getMaxRows()
			});
			
			if(pager != null) {
				var page = $('#data-table-pager').pager('getPage');
				if (page > 1) {
					var offset = (page - 1) * resultsTable.getMaxRows();
					searchRequest.queryRules.push({
						operator : 'OFFSET',
						value : offset
					});
				}
			}
		}

		var count = 0;

		if (searchQuery) {
			if(/\S/.test(searchQuery)){	
				searchRequest.queryRules.push({
					operator : 'SEARCH',
					value : searchQuery
				});
				count++;
			}
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
	
	
	molgenis.initializeAggregate = function(dataSetUri){
		selectedDataSet = restApi.get(dataSetUri, null, null);
		var queryRules = [];

		queryRules.push({
			'field' : 'dataType',
			'operator' : 'EQUALS',
			'value' : 'categorical'
		});
		queryRules.push({
			'operator' : 'OR'
		});
		queryRules.push({
			'field' : 'dataType',
			'operator' : 'EQUALS',
			'value' : 'bool'
		});
		
		queryRules.push({
			operator : 'LIMIT',
			value : 1000000
		});
		var fragments = selectedDataSet.href.split("/");
		var searchRequest = { 
			'documentType' : "protocolTree-" + fragments[fragments.length - 1],
			'featureFilters' : featureFilters,
			'queryRules' : queryRules
		};
		
		searchApi.search(searchRequest, function(searchResponse) {
			
			var searchHits = searchResponse.searchHits;
			var selectTag = $('<select id="selectFeature"/>');
		
			$.each(searchHits, function(index, hit){
				selectTag.append('<option value=' + hit.columnValueMap.id + '>' + hit.columnValueMap.name + '</option>');
			});			
			$('#feature-select').empty().append(selectTag);
			molgenis.loadAggregate(selectTag.val());
			selectTag.chosen();
			selectTag.change(function() {
				molgenis.loadAggregate($(this).val());
			});
		});
	};
	
	molgenis.loadAggregate = function(featureId){
		var searchRequest = molgenis.createSearchRequest();
		searchRequest.queryRules.push({
			'operator' : 'LIMIT',
			'value' : 1000000
		});
		var feature = restApi.get('/api/v1/observablefeature/' + featureId);
		searchRequest.fieldsToReturn = [feature.identifier];
		var aggregateRequest = {
			'documentType' : selectedDataSet.identifier,
			'featureId' : featureId,
			'searchRequest' : searchRequest,
			'dataType' : feature.dataType
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/aggregate',
			data : JSON.stringify(aggregateRequest),
			contentType : 'application/json',
			success : function(aggregateResult){
				var table = $('<table />').addClass('table table-striped');
				table.append('<tr><th>Category name</th><th>Count</th></tr>');
				$.each(aggregateResult.hashCategories, function(categoryName, count){
					table.append('<tr><td>' + categoryName + '</td><td>' + count + '</td></tr>');
				});
				$('#aggregate-table-container').empty().append(table);
			}
		});
	};
	
	molgenis.filterDialog = function(){
		var datasetId = $('#dataset-select').val();
		
		var filterDialogRequest = {
				'datasetIdentifier' : selectedDataSet.identifier,
				'datasetName' : selectedDataSet.name,
				'datasetId' : datasetId
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/filterdialog',
			data : JSON.stringify(filterDialogRequest),
			contentType : 'application/json',
			success : function(result){	
				$(function() {
					var modal = $('#filter-dialog-modal').html(result);
					
					$('#filter-dialog-modal').show();
				});

			}
		});
	}
	
	molgenis.download = function() {
		var jsonRequest = JSON.stringify(molgenis.createSearchRequest(false));
		
		parent.showSpinner();
		$.download(molgenis.getContextUrl() + '/download',{searchRequest :  jsonRequest});
		parent.hideSpinner();
	};
	
	molgenis.toggleViewer = function(statusDataViewer){
		if(statusDataViewer){
			aggregateView = false;
			$('#dataexplorer-grid-data').show();
			$('#dataDiv').removeClass("notselected").addClass("selected");
			$('#aggregateDiv').removeClass("selected").addClass("notselected");
			$('#aggregateViewIcon').attr("src","/img/aggregate-icon-notselected.png");
			$('#dataViewIcon').attr("src","/img/grid-icon.png");
			$('#aggregateDiv').css({"color":"#C0C0C0"});
			$('#dataexplorer-aggregate-data').hide();
		}
		else{
			aggregateView = true;
			$('#dataexplorer-aggregate-data').show();
			$('#dataDiv').removeClass("selected").addClass("notselected");
			$('#aggregateDiv').removeClass("notselected").addClass("selected");
			$('#dataViewIcon').attr("src","/img/grid-icon-notselected.png");
			$('#aggregateViewIcon').attr("src","/img/aggregate-icon.png");
			$('#aggregateDiv').css({"color":"black"});
			$('#dataexplorer-grid-data').hide();
		}
	};
	
	// on document ready
	$(function() {
		// use chosen plugin for data set select
		$('#dataset-select').chosen();
		$('#dataset-select').change(function() {
			var checkDataViewer = $('#dataexplorer-grid-data').is(':visible');
			restApi.getAsync($('#dataset-select').val(), null, null, function(dataSet) {
				selectedDataSet = dataSet;
				molgenis.createFeatureSelection(dataSet.protocolUsed.href);
			});
			if(checkDataViewer){
				molgenis.onDataSetSelectionChange($(this).val());
			}else
				molgenis.initializeAggregate($(this).val());
		});
		
		resultsTable = new molgenis.ResultsTable();
		
		$('#data').click(function (){
			molgenis.toggleViewer(true);
			molgenis.createFeatureSelection(selectedDataSet.protocolUsed.href);
		});
		
		$('#filter-wizard-button').click(function (){
			molgenis.filterDialog();
		});
		
		
		$('#aggregateViewIcon').attr("src","/img/aggregate-icon-notselected.png");
		$('#aggregateDiv').css({"color":"#C0C0C0"});
		
		$('#aggregate').click(function (){	
			molgenis.toggleViewer(false);
			molgenis.initializeAggregate($('#dataset-select').val());
			molgenis.createFeatureSelection(selectedDataSet.protocolUsed.href);
		});
		
		$("#observationset-search").focus();
		$("#observationset-search").change(function(e) {
			molgenis.searchObservationSets($(this).val());
		});

		$('.feature-filter-dialog').dialog({
			modal : true,
			width : 500,
			autoOpen : false
		});
		
		$('#download-button').click(function() {
			molgenis.download();
		});

		// fire event handler
		$('#dataset-select').change();
		
		$('#filter-dialog-modal').on('hide', function (e) {
			e.stopPropagation();
  		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));