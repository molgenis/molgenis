(function($, molgenis) {
	"use strict";
	
	var selectedStudyDefinitionId;
	var selectedStudyDefinitionState;
	
	// on document ready
	$(function() {
		var viewInfoContainer = $('#study-definition-viewer-info');
		var viewTreeContainer = $('#study-definition-viewer-tree');
		var editInfoContainer = $('#study-definition-editor-info');
		var editTreeContainer = $('#study-definition-editor-tree');
        var editStateSelect = $('#edit-state-select');
		var updateStudyDefinitionBtn = $('#update-study-definition-btn');
        var exportStudyDefinitionBtn = $('#export-study-definition-btn');
		
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
			createDynatreeConfigRec(catalog, dynaNodes);
			return dynaNodes;
		}
		
		/**
	     * @memberOf molgenis.studymanager
	     */
		function createCatalogInfo(catalog) {
			var items= [];
			items.push('<table class="table table-condensed table-borderless">');
			items.push('<tr><td>Version</td><td>' + (catalog.version ? catalog.version : '') + '</td></tr>');
			items.push('<tr><td>Description</td><td>' + (catalog.description ? catalog.description : '') + '</td></tr>');
			items.push('<tr><td>Authors</td><td>' + (catalog.authors ? catalog.authors.join(', ') : '') + '</td></tr>');
			items.push('</table>');
			return items.join('');
		}
		
		/**
	     * @memberOf molgenis.studymanager
	     */
		function updateStudyDefinitionTable(selectStudyDefinition) {
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/list',
				data: 'state=' + selectedStudyDefinitionState + '&search=' + encodeURIComponent($('#studydefinition-search').val()),
				success : function(data) {
                    if((selectStudyDefinition === undefined || selectStudyDefinition === true) && data.studyDefinitions.length === 0){
                        $('#study-definition-info').hide();
                    }else{
                        $('#study-definition-info').show();
                    }
					var table = $('#studyDefinitionList tbody');
					if(data.studyDefinitions.length > 0) {						
						$('#studyDefinitionList').show();
						$('#noSearchResultsMsg').hide();
						var items = [];
						$.each(data.studyDefinitions, function(idx, studyDefinition) {
						    items.push('<tr>');
						    items.push('<td class="listEntryRadio">');
						    if(studyDefinition.loaded)
						    	items.push('LOADED');
						    else
						    	items.push('<input id="catalog_' + studyDefinition.id + '" type="radio" name="id" value="' + studyDefinition.id + '">');
						    items.push('</td>');
						    items.push('<td class="listEntryId">' + studyDefinition.id + '</td>');
						    items.push('<td>' + studyDefinition.name + '</td>');
						    items.push('<td>' + (studyDefinition.email ? studyDefinition.email : '') + '</td>');
						    items.push('<td>' + (studyDefinition.date ? studyDefinition.date : '') + '</td>');
						    items.push('</tr>');
						});
						table.html(items.join(''));
					} else {
						$('#studyDefinitionList').hide();
						$('#noSearchResultsMsg').show();
					}
					
					if(selectStudyDefinition === undefined || selectStudyDefinition === true) {
						var studyDefinitionRadio = $('#studyDefinitionList input[type="radio"]:first');
						studyDefinitionRadio.attr('checked', 'checked');
						studyDefinitionRadio.change();
					}
				}
			});
		}

		/**
	     * @memberOf molgenis.studymanager
	     */
		function updateStudyDefinitionViewer() {
			// clear previous tree
			if (viewTreeContainer.children('ul').length > 0)
				viewTreeContainer.dynatree('destroy');
			viewInfoContainer.empty();
			viewTreeContainer.empty();
			viewTreeContainer.html('Loading viewer ...');

			// create new tree
			$.ajax({
				type : 'GET',
				url : molgenis.getContextUrl() + '/view/' + selectedStudyDefinitionId,
				success : function(result) {
					viewInfoContainer.html(createCatalogInfo(result.catalog));
					viewTreeContainer.empty();
					viewTreeContainer.dynatree({'minExpandLevel': 2, 'children': createDynatreeConfig(result.catalog), 'selectMode': 3, 'debugLevel': 0});
				},
				error: function (xhr) {
					viewTreeContainer.empty();
				}
			});
		}
		
		/**
	     * @memberOf molgenis.studymanager
	     */
		function updateStudyDefinitionEditor() {
                // clear previous tree
                if (editTreeContainer.children('ul').length > 0)
                    editTreeContainer.dynatree('destroy');
                editInfoContainer.empty();
                editTreeContainer.empty();
                editTreeContainer.html('Loading editor ...');
                
                // create new tree
                var checkbox = selectedStudyDefinitionState !== 'APPROVED' && selectedStudyDefinitionState !== 'EXPORTED';
                $.ajax({
                    type : 'GET',
                    url : molgenis.getContextUrl() + '/edit/' + selectedStudyDefinitionId,
                    success : function(result) {
                        editInfoContainer.html(createCatalogInfo(result.catalog));
                        editTreeContainer.empty();
                        editTreeContainer.dynatree({'minExpandLevel': 2, 'children': createDynatreeConfig(result.catalog), 'selectMode': 3, 'debugLevel': 0, 'checkbox': checkbox});
                        editStateSelect.val(result.status);
                    },
                    error: function (xhr) {
                        editTreeContainer.empty();
                    }
                });
		}
		
		$('#studyDefinitionForm').on('change', 'input[type="radio"]', function() {
			selectedStudyDefinitionId = $(this).val();
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
		
		$('#download-study-definition-btn').click(function() {
			window.location = molgenis.getContextUrl() + '/download/' + selectedStudyDefinitionId;
		});
		
		updateStudyDefinitionBtn.click(function() {
			updateStudyDefinitionBtn.prop('disabled', true);

            // get selected nodes
            var catalogItemIds = $.map(editTreeContainer.dynatree('getTree').getSelectedNodes(), function (node) {
                if (!node.data.isFolder) {
                    return node.data.key;
                }
                return null;
            });

			// remove duplicates
			var uniquecatalogItemIds = [];
			$.each(catalogItemIds, function(i, el){
			    if($.inArray(el, uniquecatalogItemIds) === -1) uniquecatalogItemIds.push(el);
			});

			var currentStudyDefinitionState = editStateSelect.val();
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/update/' + selectedStudyDefinitionId,
				data : JSON.stringify({
                    'status': currentStudyDefinitionState,
					'catalogItemIds': uniquecatalogItemIds
				}),
				contentType : 'application/json',
				success : function(entities) {
					molgenis.createAlert([{'message': 'Updated study definition [' + selectedStudyDefinitionId + ']'}], 'success');
					selectedStudyDefinitionState = currentStudyDefinitionState;
					updateStudyDefinitionEditor();
					updateStudyDefinitionTable(false);
				},
				complete: function() {
					updateStudyDefinitionBtn.prop('disabled', false);
				}
			});
        });

        exportStudyDefinitionBtn.click(function() {
        	exportStudyDefinitionBtn.prop('disabled', true);
            $.ajax({
                type : 'POST',
                url : molgenis.getContextUrl() + '/export/' + selectedStudyDefinitionId,
                contentType : 'application/json',
                success : function(entities) {
                    molgenis.createAlert([{'message': 'Exported study definition [' + selectedStudyDefinitionId + ']'}], 'success');
                    updateStudyDefinitionTable(false);
                },
				complete: function() {
					exportStudyDefinitionBtn.prop('disabled', false);
				}
            });
        });
		
		$('#state-select').change(function(){
			selectedStudyDefinitionState = $('#state-select').val();
            if(exportStudyDefinitionBtn !== undefined){
                if(selectedStudyDefinitionState === 'APPROVED')
                {
                    exportStudyDefinitionBtn.show();
                }
                else
                {
                    exportStudyDefinitionBtn.hide();
                }
            }
			updateStudyDefinitionTable();
		});

		$('#state-select').change();
		
		$('#search-button').on('click', function(){
			updateStudyDefinitionTable();
		});
		
		$('#studydefinition-search').keydown(function(e) {
			if (e.keyCode == 13 || e.which === '13') { // enter
				e.preventDefault();
				updateStudyDefinitionTable();
			}
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));