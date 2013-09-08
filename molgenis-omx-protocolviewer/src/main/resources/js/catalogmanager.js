(function($, w) {
	"use strict";
	
	// on document ready
	$(function() {
		var infoContainer = $('#catalog-preview-info');
		var treeContainer = $('#catalog-preview-tree');
		
		function createDynatreeConfig(catalog) {
			function createDynatreeConfigRec(node, dynaNode) {
				var dynaChild = {key: node.id, title: node.name, select: node.selected, isFolder: true, children:[]};
				dynaNode.push(dynaChild);
				if(node.children) {
					$.each(node.children, function(idx, child) {
						createDynatreeConfigRec(child, dynaChild.children);
					});
				}
				if(node.items) {
					$.each(node.items, function(idx, item) {
						dynaChild.children.push({key: item.id, title: item.name, select: item.selected});
					});
				}
			}
			
			var dynaNodes = [];
			if(catalog.children) {
				$.each(catalog.children, function(idx, child) {
					createDynatreeConfigRec(child, dynaNodes);
				});
			}
			return dynaNodes;
		}
		
		$('#catalogForm input[type="radio"]').change(function() {
			if($(this).data('loaded')) {
				$('#loadButton').attr('name', 'unload');
				$('#loadButton').val('Unload');
			}
			else {
				$('#loadButton').attr('name', 'load');
				$('#loadButton').val('Load');
			}
			
			// clear previous tree
			if (treeContainer.children('ul').length > 0)
				treeContainer.dynatree('destroy');
			infoContainer.empty();
			treeContainer.empty();
			infoContainer.html('Loading preview ...');
			
			// create new catalog preview
			var catalogId = $('#catalogForm input[type="radio"]:checked').val();
			$.get('/plugin/catalogmanager/view/' + catalogId, function(catalog) {
				var items= [];
				items.push('<table class="table table-condensed table-borderless">');
				items.push('<tr><td>Version</td><td>' + catalog.version + '</td></tr>');
				items.push('<tr><td>Description</td><td>' + catalog.description + '</td></tr>');
				items.push('<tr><td>Authors</td><td>' + catalog.authors.join(', ') + '</td></tr>');
				items.push('</table>');
				infoContainer.html(items.join(''));
				
				// create new tree
				treeContainer.empty();
				treeContainer.dynatree({'minExpandLevel': 2, 'children': createDynatreeConfig(catalog), 'debugLevel': 0});
			});
		});
		$('#catalogForm input[type="radio"]:checked').change();
	});
}($, window.top));