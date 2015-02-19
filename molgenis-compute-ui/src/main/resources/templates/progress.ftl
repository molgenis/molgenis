	var PAPER_WIDTH = 600;
	var PAPER_HEIGHT = 300;
	var RECT_WIDTH = 200;
	var RECT_HEIGHT = 42;
		
	$('#paper').unbind();
	$('#paper').empty();
	
	var restApi = new window.top.molgenis.RestClient();
	var graph = new joint.dia.Graph;
	var paper = new joint.dia.Paper({
		el: $('#paper'),
		width: PAPER_WIDTH,
		height: PAPER_HEIGHT,
		model: graph,
		interactive: false
	});
		
	graph.clear();
		
	var rects = {
	<#list analysis.workflow.nodes as node>
		<#assign totalJobCount=jobCount.getTotalJobCount(node.identifier) >
        <#assign scheduledJobCount=jobCount.getScheduledJobCount(node.identifier) >
        <#assign runningJobCount=jobCount.getRunningJobCount(node.identifier) >
		<#assign completedJobCount=jobCount.getCompletedJobCount(node.identifier) >
		<#assign failedJobCount=jobCount.getFailedJobCount(node.identifier) >
			
		'${node.identifier?js_string}': new joint.shapes.basic.Rect({
			id: '${node.identifier?js_string}',
			size: { 
				width: RECT_WIDTH, 
				height: RECT_HEIGHT
			},
			attrs: {
        		rect: {
        			fill:<#if failedJobCount gt 0 >'#f2dede'<#elseif completedJobCount==0>'#ffffff'<#elseif completedJobCount==totalJobCount>'#dff0d8'<#else>'#d9edf7'</#if>,
            		cursor: 'pointer'
        		},
        		text: {
            		text: '${node.name?js_string}\n A:${totalJobCount} S:${scheduledJobCount} R:${runningJobCount} C:${completedJobCount} F:${failedJobCount}',
           	 		fill:  '#000000',
            		'font-size': 12,
            		'font-weight': 'bold', 
            		cursor: 'pointer'
        		}
    		}
		}),
	</#list>
	}

	_.each(rects, function(rect) { 
		graph.addCell(rect); 
	});
		
	<#list analysis.workflow.nodes as node>
		<#if node.previousNodes?has_content>
			<#list node.previousNodes as prevNode>
				 graph.addCell(new joint.dia.Link({
    				source: { id: rects['${prevNode.identifier?js_string}'].id },
    				target: { id: rects['${node.identifier?js_string}'].id},
    				attrs: {
        				'.connection': {
            				stroke: '#333333',
            				'stroke-width': 3
        				},
        				'.marker-target': {
            				fill: '#333333',
            				d: 'M 10 0 L 0 5 L 10 10 z'
        				}
    				}
    			}));
			</#list>
		</#if>
	</#list>

	joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });
	paper.fitToContent();
	
	var stepJobs;
	var index = 0;
		
	paper.on('cell:pointerup', function(cellView, evt, x, y) { 
		if (rects.hasOwnProperty(cellView.model.id)) {
			var node = restApi.get('/api/v1/computeui_WorkflowNode/' + cellView.model.id, {expand:[]});
			$('#step').val(node.name);
			
			index = 0;
			stepJobs = restApi.get('/api/v1/computeui_AnalysisJob', {q:{q: [{field:'workflowNode', operator: 'EQUALS', value:node.identifier}] }}).items;
			if (stepJobs.length > 0) {
				if (stepJobs.length > 1) {
					updatePager();
				} else {
					$('#jobPager').html('');
					updateModalValues();	
				}
			}
			
			$('#jobModal').modal('show');
		}
    });
    
    function pagerNext() {
		index++;
		updatePager();
	}
	
	function pagerPrev() {
		index--;
		updatePager();
	}
	
    function updatePager() {
    	var items = [];
    	
    	if (index > 0) {
    		items.push('<a href="#" id="prev" onclick="pagerPrev()" > &lt; previous </a>');
		} else {
			items.push('&lt; previous ');
		}
		
		items.push((index+1));
		items.push('-');
		items.push(stepJobs.length);
		
		if (index < stepJobs.length-1) {
			items.push('<a href="#" id="next" onclick="pagerNext()"> next &gt;</a>');
		} else {
			items.push('next &gt;');
		}
		
		updateModalValues()			
		$('#jobPager').html(items.join(''));
    }
    
    function updateModalValues() {
    	$('#job').val(stepJobs[index].name);
		$('#status').val(stepJobs[index].status);	
		
		if (stepJobs[index].outputMessage) {
			$('#message').val(stepJobs[index].outputMessage);
		} else {
			$('#message').val('');
		}
	}
