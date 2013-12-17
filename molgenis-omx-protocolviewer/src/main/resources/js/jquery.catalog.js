(function($, molgenis) {
	"use strict";

	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();

	function createTreeConfig(settings, callback) {
		function createTreeNodes(tree, subTrees, treeConfig, callback) {
			function createTreeNodesRec(tree, selectedNodes, parentNode) {
				$.each(tree, function(protocolId, subTree) {
					var protocolUri = '/api/v1/protocol/' + protocolId;
					var protocol = restApi.get(protocolUri, subTree ? ['features'] : []);
					
					// create protocol node
					var node = {
						key : protocolUri,
						title : protocol.name,
						isFolder : true,
						isLazy: subTree === null,
						expand: subTree !== null
					};
					if (protocol.description)
						node.tooltip = molgenis.i18n.get(protocol.description);
					
					// determine whether node is lazy or not
					if(subTree) {
						node.children = [];
						
						var features = {};
						if(protocol.features.items) {
							$.each(protocol.features.items, function() {
								features[parseInt(restApi.getPrimaryKeyFromHref(this.href))] = this;
							});
						}
						// create feature nodes
						$.each(subTree, function(key, val) {
							if(key.charAt(0) === 'F') {
								var feature = features[key.substring(1)];
								var featureNode = {
									key : feature.href,
									title : feature.name,
									isFolder : false,
									select: selectedNodes.hasOwnProperty(feature.href)
								};
								if (feature.description)
									featureNode.tooltip = molgenis.i18n.get(feature.description);
								node.children.push(featureNode);
							}
						});
						// recurse for subprotocols
						$.each(subTree, function(key, val) {
							if(key.charAt(0) !== 'F') {
								var subTree = {};
								subTree[key] = subTrees.hasOwnProperty(key) ? subTrees[key] : null;
								createTreeNodesRec(subTree, selectedNodes, node.children);
							}
						});
					}
					
					// append protocol node
					parentNode.push(node);
				});
			}
			
			var nodes = [];
			var selectedNodes = {};
			$.each(settings.selectedItems, function() {
				selectedNodes[this] = null;
			});
			createTreeNodesRec(tree, selectedNodes, nodes);
			treeConfig.children = nodes;
			callback(treeConfig);
		}
			
		var treeConfig = {
			selectMode : 3,
			minExpandLevel : 2,
			debugLevel : 0,
			checkbox : settings.selection,
			onPostInit : function() {
				if (settings.onInit)
					settings.onInit();
			},
			onLazyRead : function(node) {
				node.setLazyNodeStatus(DTNodeStatus_Loading);
				
				// TODO deal with multiple entity pages
				restApi.getAsync(node.data.key, [ 'features', 'subprotocols' ], null, function(protocol) {
					var children = [];
					if (protocol.subprotocols) {
						if (settings.sort)
							protocol.subprotocols.items.sort(settings.sort);
						// TODO deal with multiple entity pages
						$.each(protocol.subprotocols.items, function() {
							children.push({
								key : this.href,
								title : this.name,
								tooltip : molgenis.i18n.get(this.description),
								isFolder : true,
								isLazy : true,
								select: node.isSelected()
							});
						});
					}
					if (protocol.features) {
						if (settings.sort)
							protocol.features.items.sort(settings.sort);
						// TODO deal with multiple entity pages
						$.each(protocol.features.items, function() {
							children.push({
								key : this.href,
								title : this.name,
								tooltip : molgenis.i18n.get(this.description),
								select: node.isSelected()
							});
						});
					}
					
					node.setLazyNodeStatus(DTNodeStatus_Ok);
					node.addChild(children);
				});
			},
			onClick : function(node, event) {
				if (node.getEventTargetType(event) === 'title' || node.getEventTargetType(event) === 'icon') {
					if (node.data.isFolder) {
						if (settings.onFolderClick)
							settings.onFolderClick(node.data.key);
					} else {
						if (settings.onItemClick)
							settings.onItemClick(node.data.key);
					}
				}
			},
			onSelect : function(select, node) {
				if (node.data.isFolder) {
					if (settings.onFolderSelect)
						settings.onFolderSelect(node.data.key, select);
				} else {
					if (settings.onItemSelect)
						settings.onItemSelect(node.data.key, select);
				}
			}
		};
		
		// displayedItems: yes
		     // selectedItems: yes
		         // displaySiblings: yes
		         // displaySiblings: no
		     // selectedItems: no
		         // displaySiblings: yes
                 // displaySiblings: no
		// displayedItems: no
	         // selectedItems: yes
	             // displaySiblings: yes
	             // displaySiblings: no
	         // selectedItems: no
		         // displaySiblings: yes
                 // displaySiblings: no
		if(settings.displayedItems.length > 0) {
			// FIXME search API does not support IN query
			var searchRequest = {
				documentType : 'protocolTree-' + settings.protocolId,
				query : {
					rules :	(function() {
							var queryRules = [];
							$.each(settings.displayedItems, function(i, item) {
								if (i > 0) {
									queryRules.push({
										operator : 'OR'
									});
								}
								queryRules.push({
									field : 'id',
									operator : 'EQUALS',
									value : parseInt(restApi.getPrimaryKeyFromHref(item))
								});
							});
							return [queryRules];
						}())
				}
			};
			searchApi.search(searchRequest, function(searchResponse) {				
				var tree = {};
				var subTrees = {};
				$.each(searchResponse.searchHits, function() {
					var subTree = tree;
					$.each(this.columnValueMap.path.split('.'), function() {
						var isFeature = this.charAt(0) === 'F';
						if (!subTree[this])
							subTree[this] = isFeature ? null : {};
						subTree = subTree[this];
						if (!isFeature)
							subTrees[this] = subTree;
					});
				});
				if (settings.displaySiblings) {
					var entityIds = Object.keys(subTrees);
					if(entityIds.length > 0) {
						var q = { q: [ {
							field : 'id',
							operator : 'IN',
							value : Object.keys(subTrees)
						} ]};
						// TODO deal with multiple entity pages
						restApi.getAsync('/api/v1/protocol', [ 'features', 'subprotocols' ], q, function(protocols) {
							$.each(protocols.items, function(i, protocol) {
								var subTree = subTrees[parseInt(restApi.getPrimaryKeyFromHref(protocol.href))];
								$.each(protocol.features.items, function(i, feature) {
									if(!subTree['F' + restApi.getPrimaryKeyFromHref(feature.href)])
										subTree['F' + restApi.getPrimaryKeyFromHref(feature.href)] = null;
								});
								$.each(protocol.subprotocols.items, function(i, subprotocol) {
									if(!subTree[parseInt(restApi.getPrimaryKeyFromHref(subprotocol.href))])
										subTree[parseInt(restApi.getPrimaryKeyFromHref(subprotocol.href))] = null;
								});
							});
							createTreeNodes(tree, subTrees, treeConfig, callback);
						});
					} else {
						createTreeNodes(tree, subTrees, treeConfig, callback);
					}
				} else {
					createTreeNodes(tree, subTrees, treeConfig, callback);
				}
			});
		}		
	};
	
	function createSearchTreeConfig(query, settings, treeContainer, callback) {		
		if (query) {
			var searchRequest;
			
			var queryRules = [];
			//FIXME move tokenization to search API
			$.each($.trim(query).split(' '), function(i, term) {
				if(i > 0) queryRules.push({operator : 'AND'});
				queryRules.push({
					operator : 'SEARCH',
					value : term
				});
			});
			//FIXME get unlimited number of search results
			queryRules.push({
				operator : 'LIMIT',
				value : 1000000
			});

			searchRequest = {
				documentType : 'protocolTree-' + settings.protocolId,
				queryRules : queryRules
			};			
			searchApi.search(searchRequest, function(searchResponse){
				var visibleItems = {};
				$.each(settings.selectedItems, function() {
					visibleItems[this] = null;
				});
				$.each(searchResponse.searchHits, function() {
					visibleItems[this.id] = null;
				});
				var treeSettings = $.extend({}, settings, { displayedItems: Object.keys(visibleItems), displaySiblings: false });
				createTreeConfig(treeSettings, callback);
			});
		}	
	};
	
	// default pager settings
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

		// create catalog controls
		var items = [];
		items.push('<div class="input-append">');
		items.push('<input class="catalog-search-text" type="text" title="Enter your search term">');
		items.push('<button class="catalog-search-btn btn" type="button"><i class="icon-large icon-search"></i></button>');
		items.push('<button class="catalog-search-clear-btn btn" type="button"><i class="icon-large icon-remove"></i></button>');
		items.push('</div>');
		items.push('<div class="catalog-tree"></div>');
		items.push('<div class="catalog-search-tree"></div>');
		$('.catalog-tree', container).dynatree('destroy'); // cleanup
		container.html(items.join(''));

		// create catalog
		var settings = $.extend({}, $.fn.catalog.defaults, options);
		
		var catalogTree = $('.catalog-tree', container);
		var catalogSearchTree = $('.catalog-search-tree', container);
		var searchText = $('.catalog-search-text', container);
		var searchBtn = $('.catalog-search-btn', container);
		var searchClearBtn = $('.catalog-search-clear-btn', container);
		
		// store catalog settings
		container.data('settings', settings);
		
		// catalog plugin methods
		container.data('catalog', {
			/**
			 * Async retrieve selected items from tree. 
			 * 
			 * Dynatree does not expand lazy nodes on selection, so we retrieve all descendant features 
			 * of lazy protocol nodes through the entity REST API. 
			 */
			'getSelectedItems' : function(callback) {
				function getSelectedItemsRec(protocolUris, selectedItems, callback) {					
					var q = { q: [ {
						field : 'id',
						operator : 'IN',
						value : $.map(protocolUris, function(protocolUri) {
							return parseInt(restApi.getPrimaryKeyFromHref(protocolUri));
						})
					} ]};
					// TODO deal with multiple entity pages
					restApi.getAsync('/api/v1/protocol', [ 'features', 'subprotocols' ], q, function(protocols) {
						if (protocols.items && protocols.items.length > 0) {
							var subprotocolUris = [];
							$.each(protocols.items,
									function() {
										var self = this;
										if (self.subprotocols && self.subprotocols.items
												&& self.subprotocols.items.length > 0) {
											$.each(self.subprotocols.items, function() {
												subprotocolUris.push(this.href);
											});
										}
										if (self.features && self.features.items && self.features.items.length > 0) {
											$.each(self.features.items, function() {
												selectedItems.push({'item': this.href, 'parent': self.href});
											});
										}
									});
							if (subprotocolUris.length > 0) {
								getSelectedItemsRec(subprotocolUris, selectedItems, callback);
							} else {
								callback(selectedItems);
							}
						}
					});
				}
				
				// retrieve all selected nodes (items and folders)
				var selectedNodes = catalogTree.dynatree('getTree').getSelectedNodes();
				if(selectedNodes.length > 0) {
					// divide nodes in selected items and lazy folders
					var selectedItems = [];
					var selectedLazyNodes = [];
					$.each(selectedNodes, function() {
						if(this.data.isFolder) {
							if(this.isLazy()) {
								selectedLazyNodes.push(this);
							}
						} else {
							selectedItems.push({'item': this.data.key, 'parent': this.getParent().data.key});
						}
					});
					if(selectedLazyNodes.length == 0) {
						// no lazy folders
						callback(selectedItems);
					} else {
						// determine all items for lazy folders
						getSelectedItemsRec($.map(selectedLazyNodes, function(node){ return node.data.key;}), selectedItems, callback);
					}
				} else {
					// no selected nodes
					callback([]);
				}
			},
			selectItem : function(options) {
				// TODO if item does not exist load item
				catalogTree.dynatree('getTree').getNodeByKey(options.feature).select(options.select);
			}
		});

		// register event handlers
		searchText.keyup(function(e) {
			e.preventDefault();
			if (e.keyCode == 13 || e.which === '13') { // enter
				searchBtn.click();
			}
		});

		searchBtn.click(function(e) {
			e.preventDefault();
			createSearchTreeConfig(searchText.val(), settings, container, function(treeConfig) {				
				if(!catalogSearchTree.is(':empty')) {
					catalogSearchTree.dynatree('destroy');
					catalogSearchTree.empty();
				}
				if(catalogTree.is(':visible')) catalogTree.hide();
				catalogSearchTree.dynatree(treeConfig);
				if(catalogSearchTree.is(':hidden')) catalogSearchTree.show();
			});
		});

		searchClearBtn.click(function(e) {
			e.preventDefault();
			if(!catalogSearchTree.is(':empty')) {
				if(catalogSearchTree.is(':visible')) catalogTree.hide();
				catalogSearchTree.dynatree('destroy');
				catalogSearchTree.empty();
				if(catalogTree.is(':hidden')) catalogSearchTree.show();
			}
		});
		
		// create tree
		var displayedItems = settings.selectedItems.length > 0 ? settings.selectedItems : ['/api/v1/protocol/' + settings.protocolId];
		createTreeConfig($.extend({}, settings, {displayedItems: displayedItems, displaySiblings: true}), function(treeConfig) {
			catalogTree.dynatree(treeConfig);
		});

		return this;
	};

	// default pager settings
	$.fn.catalog.defaults = {
		'protocolId' : null,
		'selection' : false,
		'selectedItems' : null,
		'sort' : null,
		'onInit' : null,
		'onFolderClick' : null,
		'onItemClick' : null,
		'onFolderSelect' : null,
		'onItemSelect' : null
	};
}($, window.top.molgenis = window.top.molgenis || {}));