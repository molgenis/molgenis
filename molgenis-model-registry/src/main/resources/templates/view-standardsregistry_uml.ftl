<#if molgenisPackage??>
	var PAPER_WIDTH = 1200;
	var PAPER_HEIGHT = 600;
	$('#paper').unbind();
	$('#paper').empty();
	
	var graph = new joint.dia.Graph;
	var uml = joint.shapes.uml;
	var paper = new joint.dia.Paper({
		el: $('#paper'),
		width: PAPER_WIDTH,
		height: PAPER_HEIGHT,
		model: graph
	});
	
	var classes = {
		<@listClasses package=molgenisPackage />
	}
	
	_.each(classes, function(c) { graph.addCell(c); });
	
	<@addVertices package=molgenisPackage />

	joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });
	
	//Calculate scale for the model size
	paper.fitToContent();
	var bbox = paper.getContentBBox();
	var scale = PAPER_WIDTH / bbox.width;
	
	//Scale the model to fit into view
	paper.setDimensions(PAPER_WIDTH, PAPER_HEIGHT);
	paper.scaleContentToFit();
	
	//Add titles to the uml classes
	$('.Class').each(function(index, el) {
		var title = document.createElementNS("http://www.w3.org/2000/svg","title");
		title.innerHTML = $('[id="' + el.id + '"] .uml-class-name-text').text();
		$(el).append($(title));
	});
	
	$('.Abstract').each(function(index, el) {
		var title = document.createElementNS("http://www.w3.org/2000/svg","title");
		title.innerHTML = $('[id="' + el.id + '"] .uml-class-name-text tspan:last').text();
		$(el).append($(title));
	});
</#if>

<#macro addVertices package>
	<#list package.entityMetaDatas as emd>
		<#if emd.extends??>
			if (classes['${emd.extends.name?replace("-", "_")?js_string}']) {
				graph.addCell(new uml.Generalization({ source: { id: classes.${emd.name?replace("-", "_")?js_string}.id }, target: { id: classes.${emd.extends.name?replace("-", "_")?js_string}.id }}));	  
			}
		</#if>
		<#list emd.attributes as amd>
			<#if amd.dataType == 'xref' || amd.dataType == 'mref' || amd.dataType == 'categorical'>
				if (classes['${amd.refEntity.name?replace("-", "_")?js_string}']) {
				  graph.addCell(new uml.Aggregation({ source: { id: classes.${emd.name?replace("-", "_")?js_string}.id }, target: { id: classes.${amd.refEntity.name?replace("-", "_")?js_string}.id }}));	  
				}
			</#if>
		</#list>
		<#list package.subPackages as p>
			<@addVertices package=p />
		</#list>
	</#list>
</#macro>

<#macro listClasses package classes=[]>
	<#list package.entityMetaDatas as emd>
		<#if emd.abstract == true >
			${emd.name?replace("-", "_")?js_string}: new uml.Abstract({
	      	 	size: { width: 180, height: ${(50 + 12 * emd.attributes?size)?c} },
	        	name: '${emd.simpleName}',
	       		attributes: [<#list emd.attributes as amd>'${amd.name?js_string}: ${amd.dataType}'<#if amd_has_next>,</#if></#list>]
	    	}),
		<#else>
			${emd.name?replace("-", "_")?js_string}: new uml.Class({
	      	 	size: { width: 180, height: ${(50 + 12 * emd.attributes?size)?c} },
	        	name: '${emd.simpleName}',
	       	 	attributes: [<#list emd.attributes as amd>'${amd.name?js_string}: ${amd.dataType}'<#if amd_has_next>,</#if></#list>]
	    	}),
	    </#if>
	</#list>
	<#list package.subPackages as p>
		<@listClasses package=p classes=classes />
	</#list>
</#macro>

	
	