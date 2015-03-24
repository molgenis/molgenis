(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	function createChildren(attributes, refEntityDepth, doSelect) {
		var children = [];
		
		$.each(attributes, function() {		
			
			var isFolder = false;		
			var classes = null;
			
			if (this.fieldType === 'MREF' || this.fieldType === 'XREF'){
				var maxDepth = $.fn.tree.defaults.maxRefEntityDepth;
				if (maxDepth >= 0){
					isFolder = refEntityDepth < maxDepth ? true : false;
				}else{
					isFolder = true;
				}
				if (isFolder) classes = 'refentitynode';
			}
			
			var isFolder = isFolder || this.fieldType === 'COMPOUND';
			
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
				},
				'refEntityDepth': refEntityDepth,
				'extraClasses': classes
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
		var items = [];
		items.push('<div class="row">');
		items.push('<div class="col-md-12">');
		items.push('<div class="molgenis-tree-controls">');
		items.push('<a href="#" class="btn btn-link pull-right tree-deselect-all-btn">Deselect all</a>');
		items.push('<a href="#" class="btn btn-link pull-right tree-select-all-btn">Select all</a>');
		items.push('</div>');
		items.push('</div>');
		items.push('</div>');
		items.push('<div class="row">');
		items.push('<div class="col-md-12">');
		items.push('<div class="molgenis-tree">');
		items.push('</div>');
		items.push('</div>');
		items.push('</div>');
		container.html(items.join(''));

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
			},
			'getNodeByUri' : function(uri) {
				return tree.fancytree('getTree').getNodeByKey(uri);
			}
		});
		
		var treeConfig = {
			'selectMode' : 3,
			'minExpandLevel' : 1,
			'debugLevel' : 0,
			'checkbox' : true,
			'keyPathSeparator' : '|',
			'init' : function(e, data) {
				if (settings.onInit)
					settings.onInit();
				
				if (data.tree.getFirstChild()) {
					data.tree.getFirstChild().setActive(true);
				}
			},
			'lazyLoad' : function (e, data) {
				var node = data.node;
				
				var target;
				var increaseDepth = 0;
				if (node.data.attribute.fieldType === "MREF" || node.data.attribute.fieldType === "XREF"){
					target = node.data.attribute.refEntity.href;
					increaseDepth = 1;
				}else{
					target = node.key;
				}
	
				data.result = $.Deferred(function (dfd) {
					restApi.getAsync(target, {'expand': ['attributes']}, function(attributeMetaData) {
						var children = createChildren(attributeMetaData.attributes, node.data.refEntityDepth + increaseDepth, function() {
							return node.selected;
						});
						dfd.resolve(children);
					});
				});	
			},
			'source' : createChildren(settings.entityMetaData.attributes, 0, function(attribute) {
				return settings.selectedAttributes ? $.inArray(attribute, settings.selectedAttributes) !== -1  : false;
			}),
			'click' : function(e, data) {
				if (data.targetType === 'title' || data.targetType === 'icon') {
					if (settings.onAttributeClick)
						settings.onAttributeClick(data.node.data.attribute);
				}
			},
			'select' : function(e, data) {
				if (settings.onAttributesSelect)
					settings.onAttributesSelect({'attribute': data.node.data.attribute, 'select': data.node.selected});
			}
		};
		tree.fancytree(treeConfig);
		
		$('.tree-select-all-btn', container).click(function(e) {
			e.preventDefault();
			
			var fn = settings.onAttributesSelect; // store handler
			settings.onAttributesSelect = null; // suppress events
			
			var selects = [];
			tree.fancytree("getRootNode").visit(function(node) {
				if(!node.isSelected()) {
					node.setSelected(true);
					selects.push({'attribute': node.data.attribute, 'select': true});
				}
			});
			
			settings.onAttributesSelect = fn; // restore handler
			
			// fire event for new selects
			if (selects.length > 0)
				settings.onAttributesSelect(selects);
		});
		
		$('.tree-deselect-all-btn', container).click(function(e) {
			e.preventDefault();
			
			var fn = settings.onAttributesSelect; // store handler
			settings.onAttributesSelect = null; // suppress events
			
			var selects = [];
			tree.fancytree("getRootNode").visit(function(node) {
				if(node.isSelected()) {
					node.setSelected(false);
					selects.push({'attribute': node.data.attribute, 'select': false});
				}
			});
			
			settings.onAttributesSelect = fn; // restore handler
			
			// fire event for new deselects
			if (selects.length > 0)
				settings.onAttributesSelect(selects);
		});
		
		return this;
	};

	// default tree settings
	$.fn.tree.defaults = {
		'entityMetaData' : null,
		'selectedAttributes' : null,
		'focusedAttribute' : null,
		'icon' : null,
		'onAttributeClick' : null,
		'onAttributesSelect' : null,
		'maxRefEntityDepth': 0	// -1 = infinite depth
							   	//  0 = default behavior (no expanding refEntities)
							   	// >0 = nr. of nested refEntities that can be expanded
	};
}($, window.top.molgenis = window.top.molgenis || {}));