(function($, molgenis) {
	"use strict";
	
	// on document ready
	$(function() {
		var infoContainer = $('#catalog-preview-info');
		var treeContainer = $('#catalog-preview-tree');
		
		function createTreeConfig(catalog) {
			function createTreeConfigRec(node, treeNode, expanded) {
				var treeChild = {key: node.id, title: node.name, expanded: expanded, selected: node.selected, folder: true, children:[]};
				treeNode.push(treeChild);
				if(node.children) {
					$.each(node.children, function(idx, child) {
						createTreeConfigRec(child, treeChild.children, false);
					});
				}
				if(node.items) {
					$.each(node.items, function(idx, item) {
						treeChild.children.push({key: item.id, title: item.name, selected: item.selected});
					});
				}
			}
			
			var treeNodes = [];
			if(catalog.children) {
				$.each(catalog.children, function(idx, child) {
					createTreeConfigRec(child, treeNodes, true);
				});
			}
			return treeNodes;
		}
		
		$('#catalogForm input[type="radio"]').change(function() {
			$('#activationButton').attr('disabled', 'disabled');
			
			if($(this).data('activated')) {
				$('#activationButton').attr('name', 'deactivate');
				$('#activationButton').val('Deactivate');
			}
			else {
				$('#activationButton').attr('name', 'activate');
				$('#activationButton').val('Activate');
			}
			
			// clear previous tree
			if (treeContainer.children('ul').length > 0)
				treeContainer.fancytree('destroy');
			infoContainer.empty();
			treeContainer.empty();
			infoContainer.html('Loading preview ...');
			
			// create new catalog preview
			var catalogId = $('#catalogForm input[type="radio"]:checked').val();
			$.ajax({
				type : 'GET',
				url : molgenis.getContextUrl() + '/view/' + catalogId,
				success : function(catalog) {
					var items= [];
					items.push('<table class="table table-condensed table-borderless">');
					items.push('<tr><td>Version</td><td>' + catalog.version + '</td></tr>');
					items.push('<tr><td>Description</td><td>' + catalog.description + '</td></tr>');
					items.push('<tr><td>Authors</td><td>' + catalog.authors.join(', ') + '</td></tr>');
					items.push('</table>');
					infoContainer.html(items.join(''));
					
					// create new tree
					treeContainer.empty();
					treeContainer.fancytree({'minExpandLevel': 2, 'source': createTreeConfig(catalog), 'debugLevel': 0});
					treeContainer.fancytree('getTree').getRootNode().sortChildren(function(a, b) { 
						if(a.folder && !b.folder) return -1;
						else if(!a.folder && b.folder) return 1;
						else return molgenis.naturalSort(a.title, b.title);
					}, true);
					
					$('#activationButton').removeAttr('disabled');
				},
				error: function (xhr) {
					treeContainer.empty();
				}
			});
		});
		$('#catalogForm input[type="radio"]:checked').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));