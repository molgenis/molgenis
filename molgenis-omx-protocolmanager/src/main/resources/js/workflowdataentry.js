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
		var steps = workflow.workflowElements;
		if(steps.length > 0) {
			// create tab menu
			var items = [];
			$.each(steps, function(i, step) {
				items.push('<li class="workflow-tab">');
				items.push('<a href="#workflow-step-pane-'+ step.id + '" data-toggle="tab" data-application=' + workflow.id + ' data-step=' + step.id + '>' + step.name + '</a>');
				items.push('</li>');
			});
			$('#workflow-nav').html(items.join(''));
			
			// create empty tab panes
			var content = [];
			$.each(steps, function(i, step) {
				content.push('<div class="tab-pane well" id="workflow-step-pane-'+ step.id + '"></div>');
			});
			$('#workflow-nav-content').html(content.join(''));
			$('#workflow-wizard').bootstrapWizard({
				'tabClass': 'bwizard-steps'
			});
			// active first tab
			$('#workflow-nav a:first').tab('show');
		}
	};
	
	ns.createWorkflowStepContainer = function(workflowId, workflowStepId, container) {
		$.ajax({
			url: plugin_uri + '/workflow/' + workflowId + '/element/' + workflowStepId,
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
			ns.onWorkflowSelectionChange($(this).val());
		});
		
		$('#workflow-nav').on('show', 'a[data-toggle="tab"]', function(e){
			var workflowApplicationId = $(e.target).data('application');
			var workflowStepId = $(e.target).data('step');
			ns.createWorkflowStepContainer(workflowApplicationId, workflowStepId, $('#workflow-step-pane-' + workflowStepId));
		});
		
		// fire event handler
		$('#workflow-application-select').change();
	});
}($, window.top));