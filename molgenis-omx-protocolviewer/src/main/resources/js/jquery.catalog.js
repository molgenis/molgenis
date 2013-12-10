(function($, molgenis) {
	"use strict";

	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();

	function createTreeConfig(settings, callback) {
		function createChildren(node, callback) {
			// TODO deal with multiple entity pages
			restApi.getAsync(node.data.key, [ 'features', 'subprotocols' ], null, function(protocol) {
				var children = [];
				if (protocol.subprotocols) {
					if (settings.sort) {
						protocol.subprotocols.items.sort(settings.sort);
					}
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
					if (settings.sort) {
						protocol.features.items.sort(settings.sort);
					}
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
				callback(children);
			});
		}
		
		var treeConfig = {
			selectMode : 3,
			minExpandLevel : 2,
			debugLevel : 0,
			checkbox: settings.selection,
			onLazyRead: function(node){
				node.setLazyNodeStatus(DTNodeStatus_Loading);
				createChildren(node, function(children) {
					node.setLazyNodeStatus(DTNodeStatus_Ok);
					node.addChild(children);
				});
			},
			onClick : function(node, event) {
				if (node.getEventTargetType(event) === 'title' || node.getEventTargetType(event) === 'icon') {
					if (node.data.isFolder) {
						if (settings.onFolderClick) {
							settings.onFolderClick(node.data.key);
						}
					} else {
						if (settings.onItemClick) {
							settings.onItemClick(node.data.key);
						}
					}
				}
			},
			onSelect : function(select, node) {				
				if (node.data.isFolder) {
					if (settings.onFolderSelect) {
						settings.onFolderSelect(node.data.key, select);
					}
				} else {
					if (settings.onItemSelect) {
						settings.onItemSelect(node.data.key, select);
					}
				}
			}
		};
		
		// TODO deal with multiple entity pages
		restApi.getAsync('/api/v1/protocol/' + settings.protocolId, [ 'features', 'subprotocols' ], null, function(protocol) {
			treeConfig.children = [ {
				key : protocol.href,
				title : protocol.name,
				icon : false,
				isFolder : true,
				isLazy : true,
				hideCheckbox: true
			}];
			callback(treeConfig);
		});
		
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
				var selectedItems = {};
				$.each(settings.selectedItems, function(i, item) {
					selectedItems[item] = null;
				});
				$.each(searchResponse.searchHits, function(i, hit) {
					selectedItems[searchHits.id] = null;
				});
				var treeSettings = { selectedItems: Object.keys(selectedItems), showSelectedOnly: false };
				createTreeConfig('/api/v1/protocol/' + settings.protocolId, treeSettings, callback);
			});
		}	
	};
	
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
			getSelectedItems : function(callback) {
				
				function getSelectedItemsRec(protocolUris, selectedItems, callback) {
					function hrefToId(href) {
						return href.substring(href.lastIndexOf('/') + 1);
					}
					
					var q = { q: [ {
						field : "id",
						operator : "IN",
						value : $.map(protocolUris, function(protocolUri) {
							return hrefToId(protocolUri);
						})
					} ]};
					// TODO deal with multiple entity pages
					restApi.getAsync('/api/v1/protocol', [ 'features', 'subprotocols' ], q, function(protocols) {
						if (protocols.items && protocols.items.length > 0) {
							var subprotocolUris = [];
							$.each(protocols.items,
									function() {
										if (this.subprotocols && this.subprotocols.items
												&& this.subprotocols.items.length > 0) {
											$.each(this.subprotocols.items, function() {
												subprotocolUris.push(this.href);
											});
										}
										if (this.features && this.features.items && this.features.items.length > 0) {
											$.each(this.features.items, function() {
												selectedItems.push(this.href);
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
							selectedItems.push(this.data.key);
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
		createTreeConfig($.extend({}, settings, {showSelectedOnly: false}), function(treeConfig) {
			catalogTree.dynatree(treeConfig);
			// expand one level of nodes
			var children = catalogTree.dynatree('getRoot').getChildren();
			if(children != null) {
				$.each(children, function() {
					if (!this.isExpanded()) {
						this.toggleExpand();
					}
				});
			}
		});

		return this;
	};

	// default pager settings
	$.fn.catalog.defaults = {
		'protocolId' : null,
		'selection' : false,
		'selectedItems' : null,
		'sort' : null,
		'onFolderClick' : null,
		'onItemClick' : null,
		'onFolderSelect' : null,
		'onItemSelect' : null
	};
}($, window.top.molgenis = window.top.molgenis || {}));