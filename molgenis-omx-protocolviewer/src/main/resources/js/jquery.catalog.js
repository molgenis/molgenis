(function($, molgenis) {
	"use strict";

	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();

	$.fn.catalog = function(options) {
		var container = this;

		// call pager method
		if (typeof options == 'string') {
			var args = Array.prototype.slice.call(arguments, 1);
			if (args.length === 0)
				return container.data('catalog')[options]();
			else if (args.length === 1)
				return container.data('catalog')[options](args[0]);
		}

		// cleanup
		$('.catalog-tree', container).dynatree('destroy');
		container.empty();

		// create catalog
		var settings = $.extend({}, $.fn.catalog.defaults, options);

		// catalog plugin methods
		container.data('catalog', {
			getSelectedItems : function() {
				return $.map($('.catalog-tree', container).dynatree('getTree').getSelectedNodes(), function(node) {
					if (!node.data.isFolder) {
						return node.data.key;
					}
					return null;
				});
			},
			selectItem : function(options) {
				$('.catalog-tree', container).dynatree('getTree').getNodeByKey(options.feature).select(options.select);
			}
		});

		var doSearch = false;
		var updatedNodes = null;
		var selectedAllNodes = null;
		var selectedNodeIds = null;
		var treePrevState = null;
		var searchQuery = null;

		var items = [];
		items.push('<div class="input-append">');
		items.push('<input class="catalog-search-text" type="text" title="Enter your search term">');
		items.push('<button class="catalog-search-btn btn" type="button"><i class="icon-large icon-search"></i></button>');
		items.push('<button class="catalog-search-clear-btn btn" type="button"><i class="icon-large icon-remove"></i></button>');
		items.push('</div>');
		items.push('<div class="catalog-tree"></div>');
		this.append(items.join(''));

		var treeContainer = $('.catalog-tree', this);

		function createChildren(protocolUri, featureOpts, protocolOpts) {
			var protocol = restApi.get(protocolUri, [ 'features', 'subprotocols' ]);
			var children = [];
			if (protocol.subprotocols) {
				if (settings.sort) {
					protocol.subprotocols.items.sort(settings.sort);
				}
				// TODO deal with multiple entity pages
				$.each(protocol.subprotocols.items, function() {
					children.push($.extend({
						key : this.href,
						title : this.name,
						tooltip : molgenis.i18n.get(this.description),
						isFolder : true,
						isLazy : protocolOpts.expand != true,
						children : protocolOpts.expand ? createChildren(this.href, featureOpts, protocolOpts) : null
					}, protocolOpts));
				});
			}
			if (protocol.features) {
				if (settings.sort) {
					protocol.features.items.sort(settings.sort);
				}

				// TODO deal with multiple entity pages
				$.each(protocol.features.items, function() {
					children.push($.extend({
						key : this.href,
						title : this.name,
						tooltip : molgenis.i18n.get(this.description)
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

		// create catalog async
		restApi.getAsync('/api/v1/protocol/' + settings.protocolId, [ 'features', 'subprotocols' ], null, function(protocol) {
			if (treeContainer.children('ul').length > 0) {
				treeContainer.dynatree('destroy');
			}
			treeContainer.empty();
			if (typeof protocol === 'undefined') {
				treeContainer.append("<p>No features available</p>");
				return;
			}

			// render tree and open first branch
			treeContainer.dynatree({
				checkbox : settings.selection,
				selectMode : 3,
				minExpandLevel : 2,
				debugLevel : 0,
				children : [ {
					key : protocol.href,
					title : protocol.name,
					icon : false,
					isFolder : true,
					isLazy : true,
					//hideCheckbox: true, //FIXME hide first root + first level of checkboxes
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
					if (node.getEventTargetType(event) === "title" || node.getEventTargetType(event) === "icon") {
						if (node.data.isFolder) {
							settings.onFolderClick(node.data.key);
						} else {
							settings.onItemClick(node.data.key);
						}
					}
				},
				onSelect : function(select, node) {
					if (node.data.isFolder) {
						if (select) {
							expandNodeRec(node);
						}
						settings.onFolderSelect(node.data.key, select);
					} else {
						settings.onItemSelect(node.data.key, select);
					}
				}
			});

			if (settings.selectedItems.length > 0) {
				var searchRequest = {
					documentType : 'protocolTree-' + settings.protocolId,
					queryRules : (function() {
						var queryRules = [];
						$.each(settings.selectedItems, function(i, item) {
							if (i > 0) {
								queryRules.push({
									operator : 'OR'
								});
							}
							queryRules.push({
								field : 'siblingIds',
								operator : 'SEARCH',
								value : hrefToId(item)
							});
						});
						return queryRules;
					}())
				};

				// TODO create tree children in one step instead of updating children in tree above
				searchAndUpdateTree(searchRequest, protocol.href, settings.selectedItems, 'select');
			}

			//treeContainer.find(".dynatree-checkbox").first().hide(); // FIXME
		});

		function search(query) {
			if (query) {
				doSearch = true;
				query = $.trim(query);

				var terms = query.split(" ");
				var queryRules = [];
				$.each(terms, function(index, element) {
					//FIXME can searchApi do the tokenization?
					queryRules.push({
						operator : 'SEARCH',
						value : element
					});
					if (index < terms.length - 1)
						queryRules.push({
							operator : 'AND'
						});
				});

				//todo: how to unlimit the search result
				queryRules.push({
					operator : 'LIMIT',
					value : 1000000
				});
				var searchRequest = {
					documentType : "protocolTree-" + settings.protocolId,
					queryRules : queryRules
				};

				//only save the state when search occurs for first time only
				var rootNode = treeContainer.dynatree("getTree").getNodeByKey('/api/v1/protocol/' + settings.protocolId);
				if (treePrevState == null)
					treePrevState = rootNode.tree.toDict();
				if (updatedNodes == null)
					updatedNodes = {};
				var selectedFeatureIds = getSelectedNodeIds($('.catalog-tree', container).dynatree('getTree').getSelectedNodes());
				searchAndUpdateTree(searchRequest, '/api/v1/protocol/' + settings.protocolId, selectedFeatureIds, 'search');
			}
		}

		/**
		 *
		 * @param searchRequest
		 * @param rootProtocolUri
		 * @param selectedNodeIds
		 */
		function searchAndUpdateTree(searchRequest, rootProtocolUri, selectedNodeIds, mode) {

			/**
			 * Preload given protocols and features so that resulting requests to individual protocols and features will be served from cache
			 *
			 * @param protocolIds
			 * @param featureIds
			 * @param callback
			 */
			function preloadEntities(protocolIds, featureIds, callback) {
				var batchSize = 500;
				var nrProtocolRequests = Math.ceil(protocolIds.length / batchSize);
				var nrFeatureRequests = Math.ceil(featureIds.length / batchSize);
				var nrRequest = nrFeatureRequests + nrProtocolRequests;
				if (nrRequest > 0) {
					var workers = [], i;
					for (i = 0; i < nrRequest; i++) {
						workers[i] = false;
					}
					for (i = 0; i < nrRequest; i++) {
						var entityType = i < nrProtocolRequests ? "protocol" : "observablefeature";
						var ids = i < nrProtocolRequests ? protocolIds : featureIds;
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
						var expands = entityType === 'protocol' ? [ 'features', 'subprotocols' ] : null;
						restApi.getAsync('/api/v1/' + entityType, expands, q, $.proxy(function() {
							workers[this.i] = true;
							if ($.inArray(false, workers) === -1) {
								this.callback();
							}
						}, {
							"i" : i,
							"callback" : callback
						}));
					}
				} else {
					callback();
				}
			}

			/**
			 * Create dynatree nodes
			 *
			 * @param nodeIds array of nodes
			 * @param selectedNodeHrefs keys of nodes that need to be selected
			 * @param nodeChildrenIdMap maps nodes to node children
			 * @returns {Array}
			 */
			function createDynatreeNodes(nodeIds, selectedNodeHrefs, nodeChildrenIdMap) {
				//TODO make selectedNodeHrefs an array instead of map
				//TODO harmonize nodeIds (strings) and selectedNodeHrefs (ints)
				var children = [];
				$.each(nodeIds, function(i, nodeId) {
					var node = createDynatreeNode(nodeId, selectedNodeHrefs, nodeChildrenIdMap);
					var childNodeIds = nodeChildrenIdMap[nodeId];
					if (childNodeIds) {
						node.children = createDynatreeNodes(childNodeIds, selectedNodeHrefs, nodeChildrenIdMap);
					}
					children.push(node);
				});
				return children;
			}

			/**
			 * Create dynatree node
			 *
			 * @param nodeId node id ("12" for folder, "F34" for item)
			 * @param selectedNodeHrefs keys of nodes that need to be selected
			 * @returns {*}
			 */
			function createDynatreeNode(nodeId, selectedNodeHrefs, nodeChildrenIdMap) {
				var isFeature = nodeId.indexOf("F") === 0;
				if (isFeature)
					nodeId = nodeId.substring(1);
				var entity, options;
				if (isFeature) {
					entity = restApi.get('/api/v1/observablefeature/' + nodeId);
					options = {
						isFolder : false,
						select : $.inArray(entity.href, selectedNodeHrefs) !== -1
					};
				} else {
					entity = restApi.get('/api/v1/protocol/' + nodeId);
					options = {
						isFolder : true,
						isLazy : !nodeChildrenIdMap.hasOwnProperty(nodeId),
						expand : nodeChildrenIdMap.hasOwnProperty(nodeId)
					};
				}
				options = $.extend({
					key : entity.href,
					title : entity.name,
					tooltip : molgenis.i18n.get(entity.description)
				}, options);
				return options;
			}

			searchApi.search(searchRequest, function(searchResponse) {
				var protocol = restApi.get(rootProtocolUri);
				var rootNode = treeContainer.dynatree("getTree").getNodeByKey(protocol.href);

				// TODO what is this?
				if (selectedAllNodes == null) {
					selectedAllNodes = {};
					$.each(rootNode.tree.getSelectedNodes(), function(index, node) {
						selectedAllNodes[node.data.key] = node;
					});
				}

				// determine all protocols and features required to build tree
				var protocols = {};
				var features = {};
				var searchHits = searchResponse.searchHits;
				$.each(searchHits, function(index, hit) {
					var nodes = hit.columnValueMap.path.split('.');
					for ( var i = 0; i < nodes.length; i++) {
						if (nodes[i].indexOf("F") === 0)
							features[nodes[i].substring(1)] = null;
						else
							protocols[nodes[i]] = null;
					}
				});

				// preload protocols and features and build tree
				preloadEntities(Object.keys(protocols), Object.keys(features), function() {
					// construct data structures required to build tree
					var rootNodeIds = [];
					var nodeChildrenIdMap = {};
					$.each(searchHits, function(index, hit) {
						var nodes = hit.columnValueMap.path.split('.');
						for ( var i = 0; i < nodes.length; i++) {
							if (i !== 0) {
								var parentNode = nodes[i - 1];
								if (!nodeChildrenIdMap[parentNode])
									nodeChildrenIdMap[parentNode] = [];
								if ($.inArray(nodes[i], nodeChildrenIdMap[parentNode]) === -1) {
									nodeChildrenIdMap[parentNode].push(nodes[i]);
								}
							} else if ($.inArray(nodes[i], rootNodeIds) === -1)
								rootNodeIds.push(nodes[i]);

							// if hit is a protocol then include protocol features and subprotocols
							if (i === nodes.length - 1 && nodes[i].indexOf("F") !== 0) {
								if (!nodeChildrenIdMap[nodes[i]])
									nodeChildrenIdMap[nodes[i]] = [];
								var protocol = restApi.get('/api/v1/protocol/' + nodes[i], [ 'subprotocols', 'features' ]);
								if (protocol.subprotocols) {
									$.each(protocol.subprotocols.items, function(index, subprotocol) {
										nodeChildrenIdMap[nodes[i]].push(hrefToId(subprotocol.href));
									});
								}
								if (protocol.features) {
									$.each(protocol.features.items, function(index, feature) {
										nodeChildrenIdMap[nodes[i]].push('F' + hrefToId(feature.href));
									});
								}
							}
						}
					});

					if (mode === 'search') {
						// if hit is a protocol then include all protocol features and subprotocols
						$.each(searchHits, function(index, hit) {
							var nodes = hit.columnValueMap.path.split('.');
							for ( var i = 0; i < nodes.length; i++) {
								if (i === nodes.length - 1 && nodes[i].indexOf("F") !== 0) {
									if (!nodeChildrenIdMap[nodes[i]])
										nodeChildrenIdMap[nodes[i]] = [];
									var protocol = restApi.get('/api/v1/protocol/' + nodes[i], [ 'subprotocols', 'features' ]);
									if (protocol.subprotocols) {
										$.each(protocol.subprotocols.items, function(index, subprotocol) {
											nodeChildrenIdMap[nodes[i]].push(hrefToId(subprotocol.href));
										});
									}
									if (protocol.features) {
										$.each(protocol.features.items, function(index, feature) {
											nodeChildrenIdMap[nodes[i]].push('F' + hrefToId(feature.href));
										});
									}
								}
							}
						});
					} else {
						// include protocol features and subprotocols for all protocols
						$.each(nodeChildrenIdMap, function(protocolId, val) {
							var protocol = restApi.get('/api/v1/protocol/' + protocolId, [ 'subprotocols', 'features' ]);
							if (protocol.subprotocols) {
								$.each(protocol.subprotocols.items, function(index, subprotocol) {
									if ($.inArray(hrefToId(subprotocol.href).toString(), nodeChildrenIdMap[hrefToId(protocol.href)]) === -1)
										nodeChildrenIdMap[hrefToId(protocol.href)].push(hrefToId(subprotocol.href).toString());
								});
							}
							if (protocol.features) {
								$.each(protocol.features.items, function(index, feature) {
									if ($.inArray('F' + hrefToId(feature.href), nodeChildrenIdMap[hrefToId(protocol.href)]) === -1)
										nodeChildrenIdMap[hrefToId(protocol.href)].push('F' + hrefToId(feature.href));
								});
							}
						});
					}

					// create dynatree nodes
					var children = createDynatreeNodes(rootNodeIds, selectedNodeIds, nodeChildrenIdMap);
					// sort dynatree nodes
					sortNodes(children);

					// TODO figure out what code below does and rewrite
					rootNode.removeChildren();
					if (rootNodeIds.length !== 0)
						rootNode.addChild(children[0].children);
					if (treeContainer.next().length > 0)
						treeContainer.next().remove();
					if (rootNodeIds.length === 0) {
						treeContainer.hide();
						treeContainer.after('<div id="match-message">No data items were matched!</div>');
					} else
						treeContainer.show();
				});
			});
		}
		;

		function getSelectedNodeIds(selectedNodes) {
			var selectedIds = [];
			$.each(selectedNodes, function(index, element) {
				selectedIds.push(element.data.key);
			});
			return selectedIds;
		}

		function hrefToId(href) {
			return href.substring(href.lastIndexOf('/') + 1);
		}

		function sortNodes(nodes) {
			if (nodes && settings.sort) {
				nodes.sort(function(a, b) {
					return settings.sort(a.title, b.title);
				});
				$.each(nodes, function(index, node) {
					if (node.children)
						sortNodes(node.children);
				});
			}
		}

		//merge the newly selected nodes back into the previous state of tree
		function recursivelyExpand(selectedFeatures, expandedNodes, nodeData, cachedNodes) {
			var entityInfo = restApi.get(nodeData.key, [ "features", "subprotocols" ]);
			var options = null;
			if (entityInfo.features.items.length && entityInfo.features.items.length != 0) {
				$.each(entityInfo.features.items, function(index, feature) {
					options = {
						key : feature.href,
						title : feature.name,
						tooltip : molgenis.i18n(feature.description),
						isFolder : false,
						expand : true
					};
					if ($.inArray(feature.href, selectedFeatures) != -1) {
						options.select = true;
					}
					nodeData.children.push(options);
					if (cachedNodes != null) {
						var fragments = options.key.split("/");
						var id = fragments[fragments.length - 1];
						if (!cachedNodes[id])
							cachedNodes[id] = options;
					}
				});
			}
			if (entityInfo.subprotocols.items.length && entityInfo.subprotocols.items.length != 0) {
				$.each(entityInfo.subprotocols.items, function(index, protocol) {
					options = {
						key : protocol.href,
						title : protocol.name,
						tooltip : molgenis.i18n.get(protocol.description),
						isFolder : true,
						isLazy : true,
						children : []
					};
					if (expandedNodes === null || $.inArray(protocol.href, expandedNodes) != -1) {
						options.expand = true;
						nodeData.children.push(recursivelyExpand(selectedFeatures, expandedNodes, options, cachedNodes));
					} else {
						nodeData.children.push(options);
					}
					if (cachedNodes != null) {
						var fragments = options.key.split("/");
						var id = fragments[fragments.length - 1];
						if (!cachedNodes[id])
							cachedNodes[id] = options;
					}
				});
			}
			return nodeData;
		}

		function clearSearch() {
			var rootNode = treeContainer.dynatree("getTree").getRoot();
			if (treePrevState == null) {
				if (rootNode.tree.getSelectedNodes().length == 0) {
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
			if (updatedNodes.select != null) {
				$.each(updatedNodes.select, function(index, node) {
					while (!rootNode.tree.getNodeByKey(node.data.key)) {
						if (node.data.isFolder)
							expandedNodes.push(node.data.key);
						else
							selectedFeatureNodes.push(node.data.key);
						node = node.parent;
					}
					var currentNode = rootNode.tree.getNodeByKey(node.data.key);
					if (node.data.isFolder) {
						currentNode.data.children = [];
						var nodeData = recursivelyExpand(selectedFeatureNodes, expandedNodes, currentNode.data);
						sortNodes(nodeData.children);
						currentNode.removeChildren();
						currentNode.addChild(nodeData.children);
						currentNode.toggleExpand();
					} else {
						currentNode.select(true);
					}
				});
			}
			if (updatedNodes.unselect != null) {
				$.each(updatedNodes.unselect, function(index, node) {
					var currentNode = rootNode.tree.getNodeByKey(node.data.key);
					if (currentNode != null)
						currentNode.select(false);
				});
			}

			rootNode.tree.enableUpdate(prevRenderMode); // restore previous rendering state

			//reset variables
			doSearch = false;
			updatedNodes = null;
			treePrevState = null;
			selectedAllNodes = null;
			selectedNodeIds = null;
			$('.catalog-search-text', container).val("");
			treeContainer.show();
			if (treeContainer.next().length > 0)
				treeContainer.next().remove();
			//updateFeatureSelection(rootNode.tree); //TODO can we remove this safely?
		}

		// register event handlers
		$('.catalog-search-text', container).keyup(function(e) {
			e.preventDefault();
			if (e.keyCode == 13 || e.which === '13') { // enter
				$('.catalog-search-btn', container).click();
			}
		});

		$('.catalog-search-btn', container).click(function(e) {
			e.preventDefault();
			search($('.catalog-search-text', container).val());
		});

		$('.catalog-search-clear-btn', container).click(function(e) {
			e.preventDefault();
			clearSearch();
		});

		return this;
	};

	// default pager settings
	$.fn.catalog.defaults = {
		'protocolId' : null,
		'selectedItems' : null,
		'sort' : null,
		'onFolderClick' : null,
		'onItemClick' : null,
		'onFolderSelect' : null,
		'onItemSelect' : null
	};
}($, window.top.molgenis = window.top.molgenis || {}));