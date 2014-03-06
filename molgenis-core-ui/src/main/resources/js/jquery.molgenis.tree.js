(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	function createChildren(attributes, doSelect) {
		var children = [];
		$.each(attributes, function() {
			var isFolder = this.fieldType === 'COMPOUND';
			children.push({
				'key' : this.href,
				'title' : this.label,
				'tooltip' : this.description,
				'folder' : isFolder,
				'lazy' : isFolder,
				'expanded' : !isFolder,
				'selected' : doSelect(this),
				'data' : {
					'attribute' : this
				}
			});
		});
		return children;
	}
	
	$.fn.tree = function(options) {
		var container = this;
		
		// call plugin method
		if (typeof options == 'string') {
			var args = Array.prototype.slice.call(arguments, 1);
			if (args.length === 0)
				return container.data('tree')[options]();
			else if (args.length === 1)
				return container.data('tree')[options](args[0]);
		}

		// cleanup existing tree
		if($('.molgenis-tree', container).fancytree)
			$('.molgenis-tree', container).fancytree('destroy');
		
		// create tree container
		container.html('<div id="molgenis-tree" class="molgenis-tree"></div>');

		// create tree container
		var tree = $('.molgenis-tree', container);
		var settings = $.extend({}, $.fn.tree.defaults, options);
		
		// store tree settings
		container.data('settings', settings);
		
		// plugin methods
		container.data('tree', {
			'getSelectedAttributes' : function(options) {
				var selectedNodes = tree.fancytree('getTree').getSelectedNodes(true);
				return $.map(selectedNodes, function(selectedNode) {
					return selectedNode.data.attribute;
				});
			}
		});
		
		var treeConfig = {
			'selectMode' : 3,
			'minExpandLevel' : 1,
			'debugLevel' : 0,
			'checkbox' : true,
			'keyPathSeparator' : '|',
			'init' : function() {
				if (settings.onInit)
					settings.onInit();
			},
			'lazyload' : function (e, data) {
				var node = data.node;
				data.result = $.Deferred(function (dfd) {
					restApi.getAsync(node.key, {'expand': ['attributes']}, function(attributeMetaData) {
						var children = createChildren(attributeMetaData.attributes, function() {
							return node.selected;
						});
						dfd.resolve(children);
					});
				});	
			},
			'source' : createChildren(settings.entityMetaData.attributes, function(attribute) {
					return settings.selectedAttributes ? $.inArray(attribute, settings.selectedAttributes) !== -1  : false;
			}),
			'click' : function(e, data) {
				if (data.targetType === 'title' || data.targetType === 'icon') {
					if (settings.onAttributeClick)
						settings.onAttributeClick(data.node.data.attribute);
				}
			},
			'select' : function(e, data) {
				if (settings.onAttributeSelect)
					settings.onAttributeSelect(data.node.data.attribute, data.node.selected);
			}
		};
		tree.fancytree(treeConfig);
		
		return this;
	};

	// default tree settings
	$.fn.tree.defaults = {
		'entityMetaData' : null,
		'selectedAttributes' : null,
		'icon' : null,
		'onAttributeClick' : null,
		'onAttributeSelect' : null
	};
}($, window.top.molgenis = window.top.molgenis || {}));