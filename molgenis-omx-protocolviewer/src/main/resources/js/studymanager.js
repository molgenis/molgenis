(function($, w) {
	"use strict";
	
	// on document ready
	$(function() {
		var viewInfoContainer = $('#study-definition-viewer-info');
		var viewTreeContainer = $('#study-definition-viewer-tree');
		var editInfoContainer = $('#study-definition-editor-info');
		var editTreeContainer = $('#study-definition-editor-tree');
		
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
		
		function updateStudyDefinitionList() {
			
		}
		
		function updateStudyDefinitionViewer() {
			// clear previous tree
			if (viewTreeContainer.children('ul').length > 0)
				viewTreeContainer.dynatree('destroy');
			viewInfoContainer.empty();
			viewTreeContainer.empty();
			viewTreeContainer.html('Loading viewer ...');
			
			// create new tree
			var studyDefinitionId = $('#studyDefinitionForm input[type="radio"]:checked').val();
			$.ajax({
				type : 'GET',
				url : '/plugin/studymanager/view/' + studyDefinitionId,
				success : function(catalog) {				
					var items= [];
					items.push('<table class="table table-condensed table-borderless">');
					items.push('<tr><td>Version</td><td>' + catalog.version + '</td></tr>');
					items.push('<tr><td>Description</td><td>' + catalog.description + '</td></tr>');
					items.push('<tr><td>Authors</td><td>' + catalog.authors.join(', ') + '</td></tr>');
					items.push('</table>');
					viewInfoContainer.html(items.join(''));
					
					viewTreeContainer.empty();
					viewTreeContainer.dynatree({'minExpandLevel': 2, 'children': createDynatreeConfig(catalog), 'selectMode': 3, 'debugLevel': 0});
				},
				error: function (xhr, textStatus, errorThrown) {
					var errorMessage = JSON.parse(xhr.responseText).errorMessage;
				    viewTreeContainer.empty();
					$('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
				}
			});
		}
		
		function updateStudyDefinitionEditor() {
			// clear previous tree
			if (editTreeContainer.children('ul').length > 0)
				editTreeContainer.dynatree('destroy');
			editInfoContainer.empty();
			editTreeContainer.empty();
			editTreeContainer.html('Loading editor ...');
			
			// create new tree
			var studyDefinitionId = $('#studyDefinitionForm input[type="radio"]:checked').val();
			$.get('/plugin/studymanager/edit/' + studyDefinitionId, function(catalog) {
				var items= [];
				items.push('<table class="table table-condensed table-borderless">');
				items.push('<tr><td>Version</td><td>' + catalog.version + '</td></tr>');
				items.push('<tr><td>Description</td><td>' + catalog.description + '</td></tr>');
				items.push('<tr><td>Authors</td><td>' + catalog.authors.join(', ') + '</td></tr>');
				items.push('</table>');
				editInfoContainer.html(items.join(''));
				
				editTreeContainer.empty();
				editTreeContainer.dynatree({'minExpandLevel': 2, 'children': createDynatreeConfig(catalog), 'selectMode': 3, 'debugLevel': 0, 'checkbox': true});
			}).fail(function() { alert("error"); });
		}
		
		$('#studyDefinitionForm input[type="radio"]').change(function() {
			$('#plugin-container .alert').remove();
			
			if($('#study-definition-viewer').is(':visible'))
				updateStudyDefinitionViewer();
			if($('#study-definition-editor').is(':visible'))
				updateStudyDefinitionEditor();
		});
		$('a[data-toggle="tab"][href="#study-definition-viewer"]').on('show', function (e) {
			updateStudyDefinitionViewer();
		});
		$('a[data-toggle="tab"][href="#study-definition-editor"]').on('show', function (e) {
			updateStudyDefinitionEditor();
		});
		
		$('#studyDefinitionForm input[type="radio"]:checked').change();
		
		$('#update-study-definition-btn').click(function() {
			var studyDefinitionId = $('#studyDefinitionForm input[type="radio"]:checked').val();
			
			// get selected nodes
			var catalogItemIds = $.map(editTreeContainer.dynatree('getTree').getSelectedNodes(), function(node) {
				if(!node.data.isFolder){
					return node.data.key;
				}
				return null;
			});
			
			// remove duplicates
			var uniquecatalogItemIds = [];
			$.each(catalogItemIds, function(i, el){
			    if($.inArray(el, uniquecatalogItemIds) === -1) uniquecatalogItemIds.push(el);
			});
			
			$.ajax({
				type : 'POST',
				url : '/plugin/studymanager/update/' + studyDefinitionId,
				data : JSON.stringify({
					'catalogItemIds': uniquecatalogItemIds
				}),
				contentType : 'application/json',
				success : function(entities) {
					$('#plugin-container').prepend('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> Successfully updated study definition [' + studyDefinitionId + ']</div>');
				}
			});
		});
	});
}($, window.top));