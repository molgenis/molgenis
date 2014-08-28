(function($, molgenis) {
	"use strict";

	var restApi = new molgenis.RestClient();
	var maxItems = 10000;

	function createTreeConfig(settings, callback) {
		function createTreeNodes(tree, subTrees, treeConfig, callback) {
			function createTreeNodesRec(tree, selectedNodes, parentNode) {
				$.each(tree, function(protocolId, subTree) {
					var protocolUri = restApi.getHref('protocol', protocolId);
					var protocol = restApi.get(protocolUri, {'expand': subTree ? ['features'] : []});
					
					// create protocol node
					var node = {
						key : protocolUri,
						title : protocol.Name,
						folder : true,
						lazy: subTree === null,
						expanded: !settings.displaySiblings
					};
					if (protocol.description)
						node.tooltip = molgenis.i18n.get(protocol.description);
					
					// determine whether node is lazy or not
					if(subTree) {
						node.children = [];
						
						// recurse for subprotocols
						$.each(subTree, function(key, val) {
							if(key.charAt(0) !== 'F') {
								var subTree = {};
								subTree[key] = subTrees.hasOwnProperty(key) ? subTrees[key] : null;
								createTreeNodesRec(subTree, selectedNodes, node.children);
							}
						});
						if (settings.sort)
							node.children.sort(settings.sort);
						
						// create feature nodes
						var features = {};
						if(protocol.Features.items) {
							$.each(protocol.Features.items, function() {
								features[parseInt(restApi.getPrimaryKeyFromHref(this.href))] = this;
							});
						}
						
						var featureNodes = [];
						$.each(subTree, function(key, val) {
							if(key.charAt(0) === 'F') {
								var feature = features[key.substring(1)];
								var featureNode = {
									key : feature.href.toLowerCase(),
									title : feature.Name,
									folder : false,
									selected: selectedNodes.hasOwnProperty(feature.href.toLowerCase())
								};
								if (feature.description)
									featureNode.tooltip = molgenis.i18n.get(feature.description);
								featureNodes.push(featureNode);
							}
						});
						if (settings.sort)
							featureNodes.sort(settings.sort);
						node.children = node.children.concat(featureNodes);
						
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
			// disable checkboxes of root nodes
			$.each(nodes, function(i, node) {
				node.hideCheckbox = true;
				node.expanded = true;
			});
			treeConfig.source = nodes;
			callback(treeConfig);
		}
			
		var treeConfig = {
			selectMode : 3,
			minExpandLevel : 2,
			debugLevel : 0,
			checkbox : settings.selection,
			keyPathSeparator: '|',
			init : function() {
				if (settings.onInit)
					settings.onInit();
			},
			lazyload : function (e, data) {
				var node = data.node;
				data.result = $.Deferred(function (dfd) {
					restApi.getAsync(node.key + '/subprotocols?num=' + maxItems, null, function(subprotocols) {
						var children = [];
						if(subprotocols.total > subprotocols.num) {
							molgenis.createAlert([ {
								'message' : 'Protocol contains more than ' + subprotocols.num + ' subprotocols'
							} ], 'error');
						}
						
						var protocolNodes = [];
						$.each(subprotocols.items, function() {
							protocolNodes.push({
								key : this.href.toLowerCase(),
								title : this.Name,
								tooltip : molgenis.i18n.get(this.description),
								folder : true,
								lazy : true,
								selected: node.selected
							});
						});
						if (settings.sort)
							protocolNodes.sort(settings.sort);
						children = children.concat(protocolNodes);
						
						restApi.getAsync(node.key + '/features?num=' + maxItems, null, function(features) {
							if(features.total > features.num) {
								molgenis.createAlert([ {
									'message' : 'Protocol contains more than ' + features.num + ' features'
								} ], 'error');
							}
							var featureNodes = [];
							$.each(features.items, function() {
								featureNodes.push({
									key : this.href.toLowerCase(),
									title : this.Name,
									tooltip : molgenis.i18n.get(this.description),
									selected: node.selected
								});
							});
							if (settings.sort)
								featureNodes.sort(settings.sort);
							children = children.concat(featureNodes);
							dfd.resolve(children);
						});
					});
				});
			},
			click : function(e, data) {
				var node = data.node;
				if (data.targetType === 'title' || data.targetType === 'icon') {
					if (node.folder) {
						if (settings.onFolderClick)
							settings.onFolderClick(node.key);
					} else {
						if (settings.onItemClick)
							settings.onItemClick(node.key);
					}
				}
			},
			select : function(e, data) {
				var node = data.node;
				if (node.folder) {
					if (settings.onFolderSelect)
						settings.onFolderSelect(node.key, node.selected, node.getKeyPath());
				} else {
					if (settings.onItemSelect)
						settings.onItemSelect(node.key, node.selected, node.getKeyPath());
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
            $('.no-results-message').hide();
			// FIXME search API does not support IN query
			var items = [];
			$.each(settings.displayedItems, function(i, item) {
				items.push(restApi.getPrimaryKeyFromHref(item));
			});

			getDataItemsByIds(settings.protocolId, items, function(searchResponse) {				
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
						var q = {
							q : [ {
								field : 'id',
								operator : 'IN',
								value : Object.keys(subTrees)
							} ],
							num : maxItems
						};
						restApi.getAsync(restApi.getHref('protocol'), {'expand': [ 'features', 'subprotocols' ], 'q': q}, function(protocols) {
							if(protocols.total > protocols.num) {
								molgenis.createAlert([ {
									'message' : 'Maximum number of protocols reached (' + protocols.num + ')'
								} ], 'error');
							}
							
							$.each(protocols.items, function(i, protocol) {
								var subTree = subTrees[parseInt(restApi.getPrimaryKeyFromHref(protocol.href))];
								$.each(protocol.Features.items, function(i, feature) {
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
        else{
            $('.catalog-search-tree').hide();
            $('.catalog-tree').hide();
            $('.no-results-message').show();
        }
	};
	
	function getDataItemsByIds(catalogId, items, callback){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/items',
			data : JSON.stringify({'catalogId' : catalogId, 'items' : items}),
			contentType : 'application/json',
			success : function(searchResponse) {
				callback(searchResponse);
			}
		});
	}
	
	function searchItems(catalogId, queryString, callback){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/search',
			data : JSON.stringify({'catalogId' : catalogId, 'queryString' : queryString}),
			contentType : 'application/json',
			success : function(searchResponse) {
				callback(searchResponse);
			}
		});
	}
	
	function createSearchTreeConfig(query, settings, treeContainer, callback) {		
		if (query) {
			searchItems(settings.protocolId, query, function(searchResponse){
				var visibleItems = {};
				$.each(searchResponse.searchHits, function() {
					visibleItems[this.columnValueMap.id] = null;
				});
				
				var treeSettings = $.extend({}, settings, {
					displayedItems : $.map(Object.keys(visibleItems), function(visibleItem) {
						return restApi.getHref('protocol', visibleItem);
					}),
					displaySiblings : false
				});
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
		items.push('<form id="model-search-form" role="form">');
		items.push('<div class="form-group">');
		items.push('<div class="col-md-9 input-group">');
		items.push('<input class="catalog-search-text form-control" type="text" placeholder="Search" autofocus="autofocus">');
		items.push('<span class="input-group-btn">');
		items.push('<button class="catalog-search-clear-btn btn btn-default" type="button"><span class="glyphicon glyphicon-remove"></span></button>');
		items.push('<button class="catalog-search-btn btn btn-default" type="button"><span class="glyphicon glyphicon-search"></span></button>');
		items.push('</span>');
		items.push('</div>');
		items.push('</div>');
		items.push('</form>');
		items.push('<div id="catalog-tree" class="catalog-tree"></div>');
		items.push('<div id="catalog-search-tree" class="catalog-search-tree"></div>');
        items.push('<div id="no-results-message" class="no-results-message">no matching items</div>');
		$('.catalog-tree', container).fancytree('destroy'); // cleanup
		container.html(items.join(''));

		// create catalog
		var settings = $.extend({}, $.fn.catalog.defaults, options);
		
		var catalogTree = $('.catalog-tree', container);
		var catalogSearchTree = $('.catalog-search-tree', container);
        var noResultsMessage = $('.no-results-message', container);
		var searchText = $('.catalog-search-text', container);
		var searchBtn = $('.catalog-search-btn', container);
		var searchClearBtn = $('.catalog-search-clear-btn', container);
		
		// store catalog settings
		container.data('settings', settings);
		
		// catalog plugin methods
		container.data('catalog', {
			selectItem : function(options) {
				// (de)select item in catalog tree
				var node = catalogTree.fancytree('getTree').getNodeByKey(options.feature);
				if(node)
					node.setSelected(options.select);
				else {
					// load (de)selected item
					var keyPath = options.path.join('|') + '|' + options.feature;
					catalogTree.fancytree('getTree').loadKeyPath(keyPath, function(node, status){
						if(node.key === options.feature)
							node.setSelected(options.select);
					});
				}
				// (de)select item in search tree
				if(!catalogSearchTree.is(':empty')) {
					var node = catalogSearchTree.fancytree('getTree').getNodeByKey(options.feature);
					if(node)
						node.setSelected(options.select);
				}
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
			
			function selectNode(key, selected, keyPath) {
				var node = catalogTree.fancytree('getTree').getNodeByKey(key);
				if(node) node.setSelected(selected);
				else {
					catalogTree.fancytree('getTree').loadKeyPath(keyPath, function(node, status){
						if(node.key === key) node.setSelected(selected);
					});
				}
			}
			
			// connect search tree events to catalog tree events
			var treeSettings = $.extend({}, settings, {
				selectedItems : (function() {
					var nodes = catalogTree.fancytree('getTree').getSelectedNodes();
					return $.map(nodes, function(node) {
						return node.key;
					});
				})(),
				onFolderSelect : selectNode,
				onItemSelect : selectNode
			});
			
			createSearchTreeConfig(searchText.val(), treeSettings, container, function(treeConfig) {
				if(!catalogSearchTree.is(':empty')) {
					catalogSearchTree.fancytree('destroy');
					catalogSearchTree.empty();
				}
				if(catalogTree.is(':visible')) catalogTree.hide();
				catalogSearchTree.fancytree(treeConfig);
				if(catalogSearchTree.is(':hidden')) catalogSearchTree.show();
			});
		});

		searchClearBtn.click(function(e) {
			e.preventDefault();
			if(!catalogSearchTree.is(':empty')) {
				if(catalogSearchTree.is(':visible')) catalogSearchTree.hide();
				catalogSearchTree.fancytree('destroy');
				catalogSearchTree.empty();
			}
            searchtext.val('');
            if(catalogTree.is(':hidden')) catalogTree.show();
            noResultsMessage.hide();
		});
		
		// create tree
		var displayedItems = settings.selectedItems.length > 0 ? settings.selectedItems : [restApi.getHref('protocol', settings.protocolId)];
		createTreeConfig($.extend({}, settings, {displayedItems: displayedItems, displaySiblings: true}), function(treeConfig) {
			catalogTree.fancytree(treeConfig);
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