(function($, molgenis) {
	"use strict";

	var ns = molgenis;
	
	var search = false;
	var searchQuery = null;
	var updatedNodes = null;
	var selectedAllNodes = null;
	var treePrevState = null;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var allSelectedFeatures = null;
	
	ns.selectDataSet = function(id) {
		restApi.getAsync('/api/v1/dataset/' + id, null, null, function(dataSet) {
		  // update
	      setFeatureDetails(null);
	      updateFeatureSelection(null);
	      ns.createFeatureSelection(dataSet.protocolUsed.href);
	      ns.setSelectedDataSet(dataSet);
		});
	};
	
	ns.setSelectedDataSet = function(dataSet) {
		ns.selectedDataSet = dataSet;
	};
	
	ns.getSelectedDataSet = function() {
		return ns.selectedDataSet;
	};
	
	ns.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		$('#feature-filters p').remove();
		search = false;
		searchQuery = null;
		treePrevState = null;
		updatedNodes = null;
		restApi.getAsync(dataSetUri, null, null, function(dataSet) {
			ns.setSelectedDataSet(dataSet);
			ns.createFeatureSelection(dataSet.protocolUsed.href);
		});
	};
	
	ns.createFeatureSelection = function(protocolUri) {
		function expandNodeRec(node) {
			if (node.childList == undefined) {
				node.toggleExpand();
			} else {
				$.each(node.childList, function() {
					expandNodeRec(this);
				});
			}
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
						select : false
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
			container.find(".dynatree-checkbox").first().hide();
		});
	};
	
	ns.getSelectedVariables = function() {
		var tree = $('#dataset-browser').dynatree('getTree');
		var features = $.map(tree.getSelectedNodes(), function(node) {
			if(!node.data.isFolder){
				var uri = node.data.key;
				return {feature: uri.substring(uri.lastIndexOf('/') + 1)};
			}
			return null;
		});
		return features;
	};
	
	ns.getSelectedFeatures = function() {
		return allSelectedFeatures;
	};
	
	ns.searchAndUpdateTree = function(query, protocolUri) {
		
		function preloadEntities(protocolIds, featureIds, callback) {
	
			var batchSize = 500;
			var nrProtocolRequests = Math.ceil(protocolIds.length / batchSize);
			var nrFeatureRequests = Math.ceil(featureIds.length / batchSize);
			var nrRequest = nrFeatureRequests + nrProtocolRequests;
			if(nrRequest > 0){
				var workers = [], i;
				for(i = 0 ; i < nrRequest ; i++) {
					workers[i] = false;
				}
				for(i = 0 ; i < nrRequest ; i++) {
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
				selectedIds.push(hrefToId(element.data.key));
			});
			return selectedIds;
		}
		
		function findAllParents(selectedFeatureIds, callback){
			var selectedFeaturesToLoad = [];
			if(selectedFeatureIds.length > 0){
				var queryRules = [];
				$.each(selectedFeatureIds, function(index, featureId){
					if(queryRules.length !== 0){
						queryRules.push({
							operator : 'OR'
						});
					}
					queryRules.push({
						field : 'id',
						operator : 'EQUALS',
						value : featureId
					});
				});
				queryRules.push({
					operator : 'LIMIT',
					value : 10000
				});
				var dataSet = ns.getSelectedDataSet();
				var searchRequest = {
					documentType : 'protocolTree-' + hrefToId(dataSet.href),
					queryRules : queryRules
				};
				searchApi.search(searchRequest, function(searchResponse){
					$.each(searchResponse.searchHits, function(index, hit){
						selectedFeaturesToLoad.push(hit);
					});
					callback(selectedFeaturesToLoad);
				});
			}else{
				callback(selectedFeaturesToLoad);
			}
		}
		
		function hrefToId (href){
			return href.substring(href.lastIndexOf('/') + 1); 
		}
		
		searchQuery = $.trim(query);
		
		searchApi.search(ns.createSearchRequest(), function(searchResponse) {
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
			
			findAllParents(selectedFeatureIds, function(hitsToHide){
				var searchHits = searchResponse["searchHits"];
				var protocols = {};
				var features = {};
				$.each(searchHits, function(){
					var object = $(this)[0]["columnValueMap"];
					var nodes = object["path"].split(".");
					//collect all features and their ancestors using REST API first.
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
							var isFeature = nodes[i].indexOf("F") === 0;
							if(isFeature) nodes[i] = nodes[i].substring(1);
							if(!cachedNode[nodes[i]]){
								var entityInfo = null;
								var options = null;
								//this is the last node and check if this is a feature
								if(isFeature){
									entityInfo = restApi.get('/api/v1/observablefeature/' + nodes[i]);
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
									//check if the last node is protocol if so recursively adding all subNodes
									if(i === nodes.length - 1) {
										options.children = createChildren('/api/v1/protocol/' + nodes[i], null, {expand : false});
										$.each(options.children, function(index, child){
											var nodeId = child.key.substring(child.key.lastIndexOf('/') + 1);
											if(!cachedNode[nodeId]){
												if(child.isFolder) child.children = [];
												cachedNode[nodeId] = child;
											}
										});
									}
								}
								options = $.extend({
									key : entityInfo.href,
									title : entityInfo.name,
									tooltip : getDescription(entityInfo).en
								}, options);
								
								if($.inArray(entityInfo.href, selectedFeatureIds) !== -1){
									options["select"] = true;
								}
								//locate the node in dynatree and otherwise create the node and insert it
								if(i != 0){
									var parentNode = cachedNode[nodes[i-1]];
									parentNode.expand = true;
									parentNode["children"].push(options);
									cachedNode[nodes[i-1]] = parentNode;
								}
								else
									topNodes.push(options);
								cachedNode[nodes[i]] = options;
							}else{
								if (nodes[i] === entityId.toString() && i != 0) {
									var parentNode = cachedNode[nodes[i-1]];
									parentNode.expand = true;
									var childNode = cachedNode[nodes[i]];
									if($.inArray(childNode, parentNode.children) === -1){ 
										parentNode.children.push(cachedNode[nodes[i]]);
										cachedNode[nodes[i-1]] = parentNode;
									}
								}
							} 
						}
					});
					
					$.each(hitsToHide, function(index, hit){
						var object = hit.columnValueMap;
						var nodes = object["path"].split(".");
						//split the path to get all ancestors;
						for(var i = 0; i < nodes.length; i++) {
							var isFeature = nodes[i].indexOf("F") === 0;
							if(isFeature) nodes[i] = nodes[i].substring(1);
							if(cachedNode[nodes[i]] && i !== 0){
								if(isFeature) cachedNode[nodes[i]].select = true;
								else {
									var currentNode = cachedNode[nodes[i]];
									if(currentNode.children.length === 0){
										currentNode.children = createChildren('/api/v1/protocol/' + nodes[i], null, {expand : false});
										cachedNode[nodes[i]] = currentNode;
										
										$.each(currentNode.children, function(index, child){
											var nodeId = child.key.substring(child.key.lastIndexOf('/') + 1);
											if(!cachedNode[nodeId]){
												if(child.isFolder) child.children = [];
												cachedNode[nodeId] = child;
											}
										});
									}
								}
							}
						}
					});
					
					sortNodes(topNodes);
					rootNode.removeChildren();
					if(topNodes.length !== 0)
						rootNode.addChild(topNodes[0].children);
					
					if($('#dataset-browser').next().length > 0) $('#dataset-browser').next().remove();
					if(topNodes.length === 0) {
						$('#dataset-browser').hide();
						$('#dataset-browser').after('<div id="match-message">No data items were matched!</div>');
					} else $('#dataset-browser').show();
				});
			});
		});
	};
	
	ns.createSearchRequest = function(){
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
		var fragments = ns.getSelectedDataSet().href.split("/");
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
		var prevRenderMode = rootNode.tree.enableUpdate(false); // disable rendering
		
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
					sortNodes(nodeData.children);
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
		
		rootNode.tree.enableUpdate(prevRenderMode); // restore previous rendering state
		
		//reset variables
		search = false;
		updatedNodes = null;
		treePrevState = null;
		selectedAllNodes = null;
		$("#search-text").val("");
		$('#dataset-browser').show();
		if($('#dataset-browser').next().length > 0) $('#dataset-browser').next().remove();
		updateFeatureSelection(rootNode.tree);
	};

	ns.search = function(query) {
		if (query) {
			search = true;
			ns.searchAndUpdateTree(query, ns.getSelectedDataSet().protocolUsed.href);
		}
	};
	
	function sortNodes(nodes){
		if(nodes){
			nodes.sort(function(a,b){
				return naturalSort(a.title, b.title);
			});
			$.each(nodes, function(index, node){
				if(node.children) sortNodes(node.children);
			});
		}
	}
	
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
					tooltip : getDescription(this).en,
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
					tooltip : getDescription(this).en,
				}, featureOpts));
			});
		}
		return children;
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
					tooltip : getDescription(feature).en,
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
					tooltip : getDescription(protocol).en,
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
		table.append('<tr><td>' + "Identifier:" + '</td><td>' + feature.identifier + '</td></tr>');
		$.each(getDescription(feature), function(key, val){
			table.append('<tr><td>' + "Description (" + key + "):" + '</td><td>' + val + '</td></tr>');
		});
		
		table.append('<tr><td>' + "Data type:" + '</td><td>' + (feature.dataType ? feature.dataType : '') + '</td></tr>');
		if (feature.unit)
			table.append('<tr><td>' + "Unit:" + '</td><td>' + feature.unit.name + '</td></tr>');

		table.addClass('listtable feature-table');
		table.find('td:first-child').addClass('feature-table-col1');
		container.append(table);

		if (feature.categories && feature.categories.length > 0) {
			var categoryTable = $('<table class="table table-striped table-condensed" />');
			$('<thead />').append('<th>Code</th><th>Label</th><th>Description</th>').appendTo(categoryTable);
			$.each(feature.categories, function(i, category) {
				var row = $('<tr />');
				$('<td />').text(category.valueCode).appendTo(row);
				$('<td />').text(category.name).appendTo(row);
				$('<td />').text(category.description).appendTo(row);
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
			updateSelectedFeatures(null);
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
			updateSelectedFeatures(null);
			return;
		}

		var table = $('<table class="table table-striped table-condensed table-hover" />');
		$('<thead />').append('<th>Group</th><th>Variable Name</th><th>Variable Identifier</th><th>Description</th><th>Remove</th>').appendTo(table);
		$.each(nodes, function(i, node) {
			if (!node.data.isFolder) {
				var feature = restApi.get(node.data.key);
				var protocol_name = node.parent.data.title;
				var name = feature.name;
				var identifier = feature.identifier;
				var description = getDescription(feature).en;
				var row = $('<tr />').attr('id', node.data.key + "_row");
				$('<td />').text(typeof protocol_name !== 'undefined' ? protocol_name : "").appendTo(row);
				$('<td />').text(typeof name !== 'undefined' ? name : "").appendTo(row);
				$('<td />').text(typeof identifier !== 'undefined' ? identifier : "").appendTo(row);
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
		updateSelectedFeatures(features);
	}
	
	function getDescription(feature){
		var str = feature.description;
		if(str && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}'))
			return {"en": str};
		return JSON.parse(str ? str : '{}');
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
	
	function updateSelectedFeatures(features){
		allSelectedFeatures = features;
		updateShoppingCart(features);
	}

	// on document ready
	$(function() {
		$(document).on('molgenis-login', function(e, msg) {
			$('.alert').alert('close');
			$('.form_header').after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
			$('#orderdata-href-btn').removeClass('disabled');
			$('#ordersview-href-btn').removeClass('disabled');
			updateSelectedFeatures(ns.getSelectedVariables()); // session changed, update shoppingcart for already selected items
		});
		$(document).on('click', '#orderdata-href-btn', function() {
			$('#orderdata-modal-container').data("data-set", ns.getSelectedDataSet());
		});
		$(document).on('molgenis-order-placed', function(e, msg) {
			var uri = ns.getSelectedDataSet().href;
			ns.selectDataSet(uri.substring(uri.lastIndexOf('/') + 1)); // reset catalogue
			molgenis.createAlert([{'message':msg}], 'success');
			search = false;
			updatedNodes = null;
			treePrevState = null;
			selectedAllNodes = null;
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));