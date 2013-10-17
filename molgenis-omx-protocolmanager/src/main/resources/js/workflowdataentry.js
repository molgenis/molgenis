(function($, w) {
	"use strict";
	
	var ns = w.molgenis = w.molgenis || {};
	var plugin_uri = "/plugin/workflowdataentry";
	
	ns.onWorkflowSelectionChange = function(workflowId) {
		$.ajax({
			url: plugin_uri + '/workflow/' + workflowId,
			success: function(data) {
				ns.createWorkflowContainer(data);
			},
			error: function() {
				alert("todo: we have to arrange error handling");
			}
		});
	};
	
	ns.createWorkflowContainer = function(workflow) {
		$('#workflow-nav').empty();
		$('#workflow-nav-content').empty();
		var elements = workflow.workflowElements;
		if(elements.length > 0) {
			// create tab menu
			var items = [];
			$.each(elements, function(i, element) {
				items.push('<li class="workflow-tab">');
				items.push('<a href="#workflow-element-pane-'+ element.id + '" data-toggle="tab" data-workflow=' + workflow.id + ' data-element=' + element.id + '>' + element.name + '</a>');
				items.push('</li>');
			});
			$('#workflow-nav').html(items.join(''));
			
			// create empty tab panes
			var content = [];
			$.each(elements, function(i, element) {
				content.push('<div class="tab-pane workflow-element-pane well" id="workflow-element-pane-'+ element.id + '" data-workflow="' + workflow.id + '" data-element="' + element.id + '"></div>');
			});
			$('#workflow-nav-content').html(content.join(''));
			$('#workflow-wizard').bootstrapWizard({
				'tabClass': 'bwizard-steps'
			});
			// active first tab
			$('#workflow-nav a:first').tab('show');
		}
	};
	
	ns.createWorkflowElementContainer = function(workflowId, workflowElementId, container) {
		$.ajax({
			url: plugin_uri + '/workflow/' + workflowId + '/element/' + workflowElementId,
			success: function(data) {
				container.html(data);
			},
			error: function() {
				alert("todo: we have to arrange error handling");
			}
		});
	};

	// on document ready
	$(function() {
		// register event handler
		$('#workflow-application-select').change(function() {
			var workflowId = $(this).val();
			if(workflowId != null) {
				ns.onWorkflowSelectionChange(workflowId);
			}
		});
		
		$('#workflow-nav').on('show', 'a[data-toggle="tab"]', function(e){
			var workflowId = $(e.target).data('workflow');
			var workflowElementId = $(e.target).data('element');
			ns.createWorkflowElementContainer(workflowId, workflowElementId, $('#workflow-element-pane-' + workflowElementId));
		});
		$(document).on('click', '.delete-row-btn', function(e){
			var el = $(this);
			$.post(plugin_uri + '/workflowelementdatarow/' + el.data('row-id') + '?_method=DELETE')
			  .done(function(data) {
				  var pane = el.closest('.workflow-element-pane');
				  ns.createWorkflowElementContainer(pane.data('workflow'), pane.data('element'), pane);
			  })
			  .fail(function(xhr, textStatus, errorThrown) {console.log(xhr, textStatus, errorThrown);
				  var errorMessage = JSON.parse(xhr.responseText).errorMessage;
				  $('#plugin-container .alert').remove();
				  $('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
			  });
		});
		$(document).on('click', '.create-row-btn', function(e) {
			var pane = $(this).closest('.workflow-element-pane');
			var inputRowIds = null;
			if($(this).closest('tr').length > 0) {
				inputRowIds = $(this).closest('tr').data('input-rows');
			}
			
			$.post(plugin_uri + '/workflowelementdatarow', {'workflowElementId': pane.data('element'), 'workflowElementDataRowIds[]': inputRowIds})
			  .done(function(data) {
				  ns.createWorkflowElementContainer(pane.data('workflow'), pane.data('element'), pane);
			  })
			  .fail(function(xhr, textStatus, errorThrown) {console.log(xhr, textStatus, errorThrown);
				  var errorMessage = JSON.parse(xhr.responseText).errorMessage;
				  $('#plugin-container .alert').remove();
				  $('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
			  });
		});
		
		$(document).on('click', '.combine-rows-btn', function(e) {
			var selectedRows = $('input:checked');
			if(selectedRows.length <= 1) {
				$('#plugin-container .alert').remove();
				  $('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> Please select two or more rows to combine</div>');
			}
			else {
				var elementId = $(selectedRows[0]).closest('.workflow-element-pane').data('element');
				var inputRowIds = [];
				$.each(selectedRows, function(i, selectedRow) {
					inputRowIds = inputRowIds.concat($(selectedRow).closest('tr').data('input-rows').toString().split(','));
				});
				$.post(plugin_uri + '/workflowelementdatarow', {'workflowElementId': elementId, 'workflowElementDataRowIds': inputRowIds})
				  .done(function(data) {
					  var pane = $(selectedRows[0]).closest('.workflow-element-pane');
					  ns.createWorkflowElementContainer(pane.data('workflow'), pane.data('element'), pane);
				  })
				  .fail(function(xhr, textStatus, errorThrown) {console.log(xhr, textStatus, errorThrown);
					  var errorMessage = JSON.parse(xhr.responseText).errorMessage;
					  $('#plugin-container .alert').remove();
					  $('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
				  });
			}
		});
		$(document).on('change', 'input[type="text"]', function(e){
			var workflowElementDataRowId = $(this).closest('tr').data('datarow');
			var featureId = $(this).closest('td').data('feature');
			
			$.post(plugin_uri + '/workflowelementdatarow/value', {'workflowElementDataRowId': workflowElementDataRowId, 'featureId': featureId, 'rawValue': $(this).val()})
			  .done(function(data) {
				  console.log(e.target);
				  $(e.target).addClass('updated-text-input');
			  })
			  .fail(function(xhr, textStatus, errorThrown) {console.log(xhr, textStatus, errorThrown);
				  var errorMessage = JSON.parse(xhr.responseText).errorMessage;
				  $('#plugin-container .alert').remove();
				  $('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
			  });
		});
		
		// fire event handler
		$('#workflow-application-select').change();
	});
}($, window.top));