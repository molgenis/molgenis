		var PAPER_WIDTH = 600;
		var PAPER_HEIGHT = 300;
		var RECT_WIDTH = 200;
		var RECT_HEIGHT = 42;
		
		$('#paper').unbind();
		$('#paper').empty();
	
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
			<#assign totalJobCount=analysis.getTotalJobCount(node.identifier)>
			<#assign completedJobCount=analysis.getCompletedJobCount(node.identifier)>
			<#assign failedJobCount=analysis.getFailedJobCount(node.identifier)>
			
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
            			text: '${node.name?js_string}\n jobs:${totalJobCount} complete:${completedJobCount} failed:${failedJobCount}',
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
    				}}));
				</#list>
			</#if>
		</#list>
	
		joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });
		paper.fitToContent();

