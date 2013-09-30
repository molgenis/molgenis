(function($, w) {
	"use strict";
	
	var ns = w.molgenis = w.molgenis || {};
	var plugin_uri = "/plugin/workflowdataentry";
	
	ns.createWorkflowDataEntryContainer = function(workflowStep) {
		$('#workflow-nav').empty();
		$('#workflow-nav-content').empty();
		if(workflowStep.workflowProtocols && workflowStep.workflowProtocols.length > 0) {
			// create tab menu
			var items = [];
			$.each(workflowStep.workflowProtocols, function(i, workflowProtocol) {
				items.push('<li class="workflow-tab">');
				items.push('<a href="#protocol-pane-'+ workflowProtocol.id + '" data-toggle="tab" data-protocol=' + workflowProtocol.id + '>' + workflowProtocol.name + '</a>');
				items.push('</li>');
			});
			$('#workflow-nav').html(items.join(''));
			
			// create empty tab panes
			var content = [];
			$.each(workflowStep.workflowProtocols, function(i, workflowProtocol) {
				content.push('<div class="tab-pane" id="protocol-pane-'+ workflowProtocol.id + '"></div>');
			});
			$('#workflow-nav-content').html(content.join(''));
			
			// active first tab
			$('#workflow-nav a:first').tab('show');
		}
	};
	
	ns.createWorkflowProtocolDataEntryContainer = function(workflowProtocolId, container) {
		$.ajax({
			url: plugin_uri + '/protocol/' + workflowProtocolId,
			success: function(data) {
				container.html(data);
			},
			error: function() {
				alert("todo: we have to arrange error handling");
			}
		});
	};
	
	ns.onWorkflowSelectionChange = function(workflowId) {
		$.ajax({
			url: plugin_uri + '/workflowstep/' + workflowId,
			success: function(data) {
				ns.createWorkflowDataEntryContainer(data);
			},
			error: function() {
				alert("todo: we have to arrange error handling");
			}
		});
	};
	
	// on document ready
	$(function() {
		// register event handler
		$('#workflow-select').change(function() {
			ns.onWorkflowSelectionChange($(this).val());
		});
		
		$('#workflow-nav').on('show', 'a[data-toggle="tab"]', function(e){
			var protocolId = $(e.target).data('protocol');
			ns.createWorkflowProtocolDataEntryContainer(protocolId, $('#protocol-pane-' + protocolId));
		});
		
		// fire event handler
		$('#workflow-select').change();
	});
}($, window.top));