<#if molgenisPackage??>
	$('#paper').html('');
	
	var graph = new joint.dia.Graph;
	var uml = joint.shapes.uml;
	var paper = new joint.dia.Paper({
		el: $('#paper'),
		width: 1200,
		height: 600,
		gridSize: 1,
		model: graph
	});
	
	var classes = {
		<@listClasses package=molgenisPackage />
	}
	
	_.each(classes, function(c) { graph.addCell(c); });
	
	<#-- todo recursive -->
	<#list molgenisPackage.entityMetaDatas as emd>
		<#list emd.attributes as amd>
			<#if amd.dataType == 'xref' || amd.dataType == 'mref' || amd.dataType == 'categorical'>
				if (classes.${amd.refEntity.name}) {
				  graph.addCell(new uml.Aggregation({ source: { id: classes.${emd.name?replace("-", "_")?js_string}.id }, target: { id: classes.${amd.refEntity.name?replace("-", "_")?js_string}.id }}));	  
				}
			</#if>
		</#list>
	</#list>

	 joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });
	 paper.scaleContentToFit();
</#if>

<#macro listClasses package classes=[]>
	<#list package.entityMetaDatas as emd>
		${emd.name?replace("-", "_")?js_string}: new uml.Class({
	       	size: { width: 180, height: ${(50 + 12 * emd.attributes?size)?c} },
	        name: '${emd.simpleName}',
	        attributes: [<#list emd.attributes as amd>'${amd.name?js_string}: ${amd.dataType}'<#if amd_has_next>,</#if></#list>]
	    }),
	</#list>
	<#list package.subPackages as p>
		<@listClasses package=p classes=classes />
	</#list>
</#macro>

	
	