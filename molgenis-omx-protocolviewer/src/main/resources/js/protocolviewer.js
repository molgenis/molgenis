(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};
	
	var search = false;
	var searchQuery = null;
	var updatedNodes = null;
	var selectedAllNodes = null;
	var treePrevState = null;
	var selectedDataSet = null;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	
	// fill dataset select
	ns.fillDataSetSelect = function(callback) {
		restApi.getAsync('/api/v1/dataset', null, null, function(datasets) {
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
	
	ns.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		$('#feature-filters p').remove();
		search = false;
		searchQuery = null;
		treePrevState = null;
		updatedNodes = null;
		restApi.getAsync(dataSetUri, null, null, function(dataSet) {
			selectedDataSet = dataSet;
			ns.createFeatureSelection(dataSet.protocolUsed.href);
		});
	};

	ns.getSelectedDataSet = function() {
		return $(document).data('datasets').selected;
	};
	
	ns.createFeatureSelection = function(protocolUri) {
		function createChildren(protocolUri, featureOpts, protocolOpts) {
			var protocol = restApi.get(protocolUri, [ "features", "subprotocols" ]);
			var children = [];
			if (protocol.subprotocols) {
				protocol.subprotocols.items.sort(characteristicSort);
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
				protocol.features.items.sort(characteristicSort);
				// TODO deal with multiple entity pages
				$.each(protocol.features.items, function() {
					children.push($.extend({
						key : this.href,
						title : this.name,
						tooltip : this.description,
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
				return node.data.isFolder ? null : node.data.key;
			});
			ns.onFeatureSelectionChange(sortedFeatures);
		}
		
		function updateNodesInSearch(select, node){
			
			if(!updatedNodes.select){
				updatedNodes.select = {};
			}
			if(!updatedNodes.unselect){
				updatedNodes.unselect = {};
			}
			var selectKeys = Object.keys(updatedNodes.select);
			var unselectKeys = Object.keys(updatedNodes.unselect);
			
			if(select){
				if(node.data.isFolder)
					node.visit(function(subNode){
						if(subNode.isSelected()){
							if($.inArray(subNode.data.key, unselectKeys) != -1) {
								delete updatedNodes.unselect[subNode.data.key];
							}
							if($.inArray(subNode.data.key, selectKeys) == -1) updatedNodes.select[subNode.data.key] = subNode;
						}
					},false);
				else{
					if($.inArray(node.data.key, unselectKeys) != -1) delete updatedNodes.unselect[node.data.key];
					if($.inArray(node.data.key, selectKeys) == -1) updatedNodes.select[node.data.key] = node;
				}
			}
			else{
				var selectKeys = Object.keys(updatedNodes.select);
				var unselectKeys = Object.keys(updatedNodes.unselect);
				
				if(node.data.isFolder){
					node.visit(function(subNode){
						if(!subNode.isSelected()){
							if($.inArray(subNode.data.key, selectKeys) != -1) {
								delete updatedNodes.select[subNode.data.key];
							}
							if($.inArray(subNode.data.key, unselectKeys) == -1) updatedNodes.unselect[subNode.data.key] = subNode;
						}
					},false);
				}
				else{
					if($.inArray(node.data.key, selectKeys) != -1) delete updatedNodes.select[node.data.key];
					if($.inArray(node.data.key, unselectKeys) == -1) updatedNodes.unselect[node.data.key] = node;
				}
			}
		}

		restApi.getAsync(protocolUri, [ "features", "subprotocols" ], null, function(protocol) {
			var container = $('#dataset-browser');
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
						getFeature(node.data.key, function(data) {
							setFeatureDetails(data);
						});
				},
				onSelect : function(select, node) {
					if (select){
						expandNodeRec(node);
					}
					if (!node.data.isFolder)
						getFeature(node.data.key, function(data) {
							setFeatureDetails(data);
						});
					else
						setFeatureDetails(null);
					//keep track of select/unselect since search starts
					if(search){
						updateNodesInSearch(select, node);
					}
					// update feature selection
					updateFeatureSelection(node.tree);
				},
			});
		});
	};
	
	ns.getSelectedVariables = function() {
		var tree = $('#dataset-browser').dynatree('getTree');
		var features = $.map(tree.getSelectedNodes(), function(node) {
			return node.data.isFolder ? null : {feature: node.data.key};
		});
		return features;
	};
	
	ns.searchFeatureTable = function(query, protocolUri) {
		
		function preloadEntities(protocolIds, featureIds, callback) {
			
			var batchSize = 500;
			var nrProtocolRequests = Math.ceil(protocolIds.length / batchSize);
			var nrFeatureRequests = Math.ceil(featureIds.length / batchSize);
			var nrRequest = nrFeatureRequests + nrProtocolRequests;
			if(nrRequest > 0){
				var workers = [];
				for(var i = 0 ; i < nrRequest ; i++) {
					workers[i] = false;
				}
				for(var i = 0 ; i < nrRequest ; i++) {
					var entityType = i < nrProtocolRequests ?  "protocol" : "observablefeature";
					var ids = i < nrProtocolRequests ?  protocolIds : featureIds;
					var start = i < nrProtocolRequests ? i * batchSize : (i - nrProtocolRequests) * batchSize;
					var q = {
							q : [ {
								"field" : "id",
								"operator" : "IN",
								"value" : ids
							} ],
							num : batchSize,
							start : start
					};
					restApi.getAsync('/api/v1/' + entityType, null, q, $.proxy(function(){
						workers[this.i] = true;
						if($.inArray(false, workers) === -1){
							this.callback();
						}
					}, {"i" : i, "callback" : callback}));
				}
			}else{
				callback();
			}
		}
		
		function selectedNodeIds(selectedNodes){
			var selectedIds = new Array ();
			$.each(selectedNodes, function(index, element){
				selectedIds.push(element.data.key);
			});
			return selectedIds;
		}
		
		console.log("searchObservationSets: " + query);
		searchQuery = query;
		ns.searchFeatureMeta(function(searchResponse) {
			var protocol = restApi.get(protocolUri);
			var rootNode =  $('#dataset-browser').dynatree("getTree").getNodeByKey(protocol.href);
			var selectedFeatureIds = selectedNodeIds(rootNode.tree.getSelectedNodes());
			//only save the state when search occurs for first time only
			if(treePrevState == null) treePrevState = rootNode.tree.toDict();
			if(updatedNodes == null) updatedNodes = {};
			if(selectedAllNodes == null){ 
				selectedAllNodes = {};
				$.each(rootNode.tree.getSelectedNodes(), function(index, node){
					selectedAllNodes[node.data.key] = node;
				});
			}
			var searchHits = searchResponse["searchHits"];
			
			var protocols = {};
			var features = {};
			$.each(searchHits, function(){
				var object = $(this)[0]["columnValueMap"];
				var nodes = object["path"].split(".");
				//collect all features and their ancesters using restapi first.
				for(var i = 0; i < nodes.length; i++){
					if(nodes[i].indexOf("F") === 0) features[nodes[i].substring(1)] = null;
					else protocols[nodes[i]] = null;
				}
			});
			
			preloadEntities(Object.keys(protocols), Object.keys(features), function(){
				var cachedNode = {};
				var topNodes = new Array();
				$.each(searchHits, function(){
					var object = $(this)[0]["columnValueMap"];
					var nodes = object["path"].split(".");
					var entityId = object["id"];
					//split the path to get all ancestors;
					for(var i = 0; i < nodes.length; i++) {
						if(!cachedNode[nodes[i]]){
							var entityInfo = null;
							var options = null;
							var isFeature = nodes[i].indexOf("F") === 0;
							//this is the last node and check if this is a feature
							if(isFeature){
								entityInfo = restApi.get('/api/v1/observablefeature/' + nodes[i].substring(1));
								options = {
									isFolder : false,
								};
							}else{
								entityInfo = restApi.get('/api/v1/protocol/' + nodes[i]);
								options = {
									isFolder : true,
									isLazy : true,
									expand : true,
									children : []
								};
							}
							options = $.extend({
								key : entityInfo.href,
								title : entityInfo.name,
								tooltip : entityInfo.description
							}, options);
							
							if($.inArray(entityInfo.href, selectedFeatureIds) !== -1){
								options["select"] = true;
							}
							//locate the node in dynatree and otherwise create the node and insert it
							if(i != 0){
								var parentNode = cachedNode[nodes[i-1]];
								parentNode["children"].push(options);
								cachedNode[nodes[i-1]] = parentNode;
							}
							else
								topNodes.push(options);
							cachedNode[nodes[i]] = options;
						}else{
							if (nodes[i] === entityId.toString() && i != 0) {
								var parentNode = cachedNode[nodes[i-1]];
								var childNode = cachedNode[nodes[i]];
								if($.inArray(childNode, parentNode.children) === -1){ 
									parentNode.children.push(cachedNode[nodes[i]]);
									cachedNode[nodes[i-1]] = parentNode;
								}
							}
						} 
					}
				});
				$.each(topNodes, function(index, node){
					sortNodes(node);
				});
				rootNode.removeChildren();
				rootNode.addChild(topNodes);
			});
		});
	};
	
	ns.searchFeatureMeta = function(callback){
		searchApi.search(ns.createSearchRequestFeatureMeta(), callback);
	};
	
	ns.createSearchRequestFeatureMeta = function(){
		var terms = searchQuery.split(" ");
		var queryRules = new Array();
		$.each(terms, function(index, element){
			//FIXME can searchApi do the tokenization?
			queryRules.push({
				operator : 'SEARCH',
				value : element,
			});
			if(index < terms.length - 1)
				queryRules.push({
					operator : 'AND'
				});
		});
		
		//todo: how to unlimit the search result
		queryRules.push({
			operator : 'LIMIT',
			value : 1000000
		});
		var fragments = selectedDataSet.href.split("/");
		var searchRequest = {
			documentType : "protocolTree-" + fragments[fragments.length - 1],
			queryRules : queryRules
		};
		return searchRequest;
	};
	
	ns.clearSearch = function() {
		
		var rootNode = $('#dataset-browser').dynatree("getTree").getRoot();
		if(treePrevState == null) {
			if(rootNode.tree.getSelectedNodes().length == 0){
				rootNode.tree.reload();
			}
			return;
		}
		rootNode.removeChildren();
		rootNode.addChild(treePrevState.children);
		
		var selectedFeatureNodes = [];
		var expandedNodes = [];
		//check if newly selected nodes exist in previous tree
		if(updatedNodes.select != null){
			$.each(updatedNodes.select, function(index, node){
				while(!rootNode.tree.getNodeByKey(node.data.key)){
					if(node.data.isFolder)
						expandedNodes.push(node.data.key);
					else
						selectedFeatureNodes.push(node.data.key);
					node = node.parent;
				}
				var currentNode = rootNode.tree.getNodeByKey(node.data.key);
				if(node.data.isFolder){
					currentNode.data.children = new Array();
					var nodeData = recursivelyExpand(selectedFeatureNodes, expandedNodes, currentNode.data);
					sortNodes(nodeData);
					currentNode.removeChildren();
					currentNode.addChild(nodeData.children);
					currentNode.toggleExpand();
				}else{
					currentNode.select(true);
				}
			});
		}
		if(updatedNodes.unselect != null){
			$.each(updatedNodes.unselect, function(index, node){
				var currentNode = rootNode.tree.getNodeByKey(node.data.key);
				if(currentNode != null) currentNode.select(false);
			});
		}
		
		//reset variables
		search = false;
		updatedNodes = null;
		treePrevState = null;
		selectedAllNodes = null;
		$("#search-text").val("");
		updateFeatureSelection(rootNode.tree);
	};

	ns.search = function(query) {
		if (query) {
			search = true;
			ns.searchFeatureTable(query, selectedDataSet.protocolUsed.href);
		}
	};
	
	function sortNodes(node){
		if(node.children){
			node.children.sort(function(a,b){
				return naturalSort(a.title, b.title);
			});
			$.each(node.children, function(index, subNode){
				sortNodes(subNode);
			});
		}
	}
	
	//merge the newly selected nodes back into the previous state of tree
	function recursivelyExpand (selectedFeatures, expandedNodes, nodeData, cachedNodes) {
		var entityInfo = restApi.get(nodeData.key, ["features", "subprotocols"]);
		var options = null;
		if(entityInfo.features.items.length && entityInfo.features.items.length != 0){
			$.each(entityInfo.features.items, function(index, feature){
				options = {
					key : feature.href,
					title : feature.name,
					tooltip : feature.description,
					isFolder : false,
					expand : true,
				};
				if($.inArray(feature.href, selectedFeatures) != -1){
					options.select = true;
				}
				nodeData.children.push(options);
				if(cachedNodes != null){
					var fragments = options.key.split("/");
					var id = fragments[fragments.length - 1];
					if(!cachedNodes[id]) cachedNodes[id] = options; 
				}
			});
		}
		if(entityInfo.subprotocols.items.length && entityInfo.subprotocols.items.length != 0){
			$.each(entityInfo.subprotocols.items, function(index, protocol){
				options = {
					key : protocol.href,
					title : protocol.name,
					tooltip : protocol.description,
					isFolder : true,
					isLazy : true,
					children : []
				};
				if(expandedNodes === null || $.inArray(protocol.href, expandedNodes) != -1){
					options.expand = true;
					nodeData.children.push(recursivelyExpand(selectedFeatures, expandedNodes, options, cachedNodes));
				}else{
					nodeData.children.push(options);
				}
				if(cachedNodes != null){
					var fragments = options.key.split("/");
					var id = fragments[fragments.length - 1];
					if(!cachedNodes[id]) cachedNodes[id] = options; 
				}
			});
		}
		return nodeData;
	}
	
	function getFeature(id, callback) {
		var data = restApi.get(id);
		$.ajax({
			type : 'POST',
			url : '/api/v1/category?_method=GET',
			data : JSON.stringify({
				q : [ {
					"field" : "observableFeature_Identifier",
					"operator" : "EQUALS",
					"value" : data.identifier
				} ],
				num : 100
			}),
			contentType : 'application/json',
			async : false,
			success : function(entities) {
				var categories = new Array();
				$.each(entities.items, function() {
					categories.push($(this)[0]);
				});
				data["categories"] = categories;
			}
		});
		callback(data);
	}

	function setFeatureDetails(feature) {
		var container = $('#feature-details').empty();
		if (feature === null) {
			container.append("<p>Select a variable to display variable details</p>");
			return;
		}

		var table = $('<table />');
		table.append('<tr><td>' + "Name:" + '</td><td>' + feature.name + '</td></tr>');
		if (feature.i18nDescription) {
			for ( var lang in feature.i18nDescription) {
				if (!feature.i18nDescription.hasOwnProperty(lang))
					continue;
				table.append('<tr><td>' + "Description (" + lang + "):" + '</td><td>' + feature.i18nDescription[lang] + '</td></tr>');
			}
		}else if(feature.description) {
			table.append('<tr><td>' + "Description: " + '</td><td>' + feature.description + '</td></tr>');
		}

		table.append('<tr><td>' + "Data type:" + '</td><td>' + (feature.dataType ? feature.dataType : '') + '</td></tr>');
		if (feature.unit)
			table.append('<tr><td>' + "Unit:" + '</td><td>' + feature.unit.name + '</td></tr>');

		table.addClass('listtable feature-table');
		table.find('td:first-child').addClass('feature-table-col1');
		container.append(table);

		if (feature.categories) {
			var categoryTable = $('<table class="table table-striped table-condensed" />');
			$('<thead />').append('<th>Code</th><th>Label</th><th>Description</th>').appendTo(categoryTable);
			$.each(feature.categories, function(i, category) {
				var row = $('<tr />');
				$('<td />').text(category.valueCode).appendTo(row);
				$('<td />').text(category.name).appendTo(row);
				$('<td />').text(category.name).appendTo(row);
				row.appendTo(categoryTable);
			});
			categoryTable.addClass('listtable');
			container.append(categoryTable);
		}
	}

	function characteristicSort(a, b) {
		return naturalSort(a.name, b.name);
	}
	
	function updateFeatureSelection(tree) {
		var container = $('#feature-selection').empty();
		if (tree === null) {
			container.append("<p>No variables selected</p>");
			updateShoppingCart(null);
			return;
		}
		
		var nodes = tree.getSelectedNodes();
		if(search){
			nodes = [];
			var allNodes = {};
			$.each(selectedAllNodes, function(key, node){
				allNodes[key] = node;
			});
			var keys = Object.keys(allNodes);
			if(updatedNodes.select != null) 
				$.each(updatedNodes.select, function(key, node){
					if($.inArray(key, keys) == -1){
						allNodes[key] = node;
					}
				});
			if(updatedNodes.unselect != null) 
				$.each(updatedNodes.unselect, function(key, node){
					if($.inArray(key, keys) !== -1){
						delete allNodes[key];
					}
				});
			$.each(allNodes, function(index, node){
				nodes.push(node);
			});
		}
		if (nodes === null || nodes.length === 0) {
			container.append("<p>No variables selected</p>");
			updateShoppingCart(null);
			return;
		}

		var table = $('<table class="table table-striped table-condensed table-hover" />');
		$('<thead />').append('<th>Group</th><th>Variable</th><th>Description</th><th>Remove</th>').appendTo(table);
		$.each(nodes, function(i, node) {
			if (!node.data.isFolder) {
				var protocol_name = node.parent.data.title;
				var name = node.data.title;
				var description = node.data.tooltip;
				var row = $('<tr />').attr('id', node.data.key + "_row");
				$('<td />').text(typeof protocol_name !== 'undefined' ? protocol_name : "").appendTo(row);
				$('<td />').text(typeof name !== 'undefined' ? name : "").appendTo(row);
				$('<td />').text(typeof description !== 'undefined' ? description : "").appendTo(row);

				var deleteButton = $('<i class="icon-remove"></i>');
				deleteButton.click($.proxy(function() {
					tree.getNodeByKey(node.data.key).select(false);
					return false;
				}, this));
				$('<td class="center" />').append(deleteButton).appendTo(row);

				row.appendTo(table);
			}
		});
		table.addClass('listtable selection-table');
		container.append(table);
		
		var features = [];
		$.each(nodes, function(i, node) {
			if (!node.data.isFolder) {
				var featureId = node.data.key.split("/");
				features.push({feature: featureId[featureId.length - 1]});
			}
		});
		updateShoppingCart(features);
	}

	function updateShoppingCart(features) {
		if (features === null) {
			$.post('/cart/empty');
		} else {
			$.ajax({
				type : 'POST',
				url : '/cart/replace',
				data : JSON.stringify({
					'features' : features
					}),
				contentType : 'application/json'
			});
		}
	}

	// on document ready
	$(function() {
		$(document).on('molgenis-login', function(e, msg) {
			$('.alert').alert('close');
			$('.form_header').after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
			$('#orderdata-href-btn').removeClass('disabled');
			$('#ordersview-href-btn').removeClass('disabled');
			updateShoppingCart(ns.getSelectedVariables()); // session changed, update shoppingcart for already selected items
		});
		$(document).on('molgenis-order-placed', function(e, msg) {
			ns.selectDataSet(ns.getSelectedDataSet()); // reset catalogue
			$('#dataset-browser').dynatree('getRoot').select(false);
			$('.form_header').after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
		});
	});
}($, window.top));