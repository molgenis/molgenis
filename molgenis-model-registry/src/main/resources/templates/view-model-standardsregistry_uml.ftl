<#include "resource-macros.ftl"><#include "resource-macros.ftl">
<!doctype html>
<html lang="en">
<head>
    <title>Model-registry UML-viewer</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" href="/js/favicon.ico">
    <link href="<@resource_href "/css/app.css" />" rel="stylesheet">
</head>
<body>
<div id="app"></div>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}'
    }
</script>

<#--<script type=text/javascript src="<@resource_href "/js/manifest.js"/>"></script>-->
<#--<script type=text/javascript src="<@resource_href "/js/vendor.js"/>"></script>-->
<#--<script type=text/javascript src="<@resource_href "/js/app.js"/>"></script>-->
</body>
</html>


<#--<#if molgenisPackage??>-->
<#--var PAPER_WIDTH = Math.max(document.documentElement.clientWidth, window.innerWidth || 0) * 0.9;-->
<#--var PAPER_HEIGHT = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);-->
<#--var RECT_WIDTH = 180;-->

<#--$('#paper').unbind();-->
<#--$('#paper').empty();-->

<#--var graph = new joint.dia.Graph;-->
<#--var uml = joint.shapes.uml;-->
<#--var paper = new joint.dia.Paper({-->
<#--el: $('#paper'),-->
<#--width: PAPER_WIDTH,-->
<#--height: PAPER_HEIGHT,-->
<#--model: graph-->
<#--});-->

<#--var classes = {-->
    <#--<@listClasses package=molgenisPackage />-->
<#--}-->

<#--_.each(classes, function(c) { graph.addCell(c); });-->

    <#--<@addVertices package=molgenisPackage />-->

<#--joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });-->

<#--//Calculate scale for the model size-->
<#--paper.fitToContent();-->
<#--var bbox = paper.getContentBBox();-->
<#--var scaleX = PAPER_WIDTH / bbox.width;-->
<#--var scaleY = PAPER_HEIGHT / bbox.height;-->
<#--var scale = Math.min(scaleX, scaleY);-->

<#--//Scale the model to fit into view-->
<#--paper.setDimensions(PAPER_WIDTH, PAPER_HEIGHT);-->
<#--paper.scale(scale, scale);-->

<#--//Add titles to the uml classes-->

<#--$('.Class').each(function(index, el) {-->
<#--var $titleEl = $('[id="' + el.id + '"] .uml-class-name-text');-->
<#--var title = document.createElementNS("http://www.w3.org/2000/svg","title");-->
<#--title.innerHTML = $titleEl.text();-->

<#--drawEllipsisIfTextTooLong($titleEl[0], RECT_WIDTH-4);-->

<#--$(el, '.uml-class-name-text').append($(title));-->
<#--});-->

<#--$('.Abstract').each(function(index, el) {-->
<#--var $titleEl = $('[id="' + el.id + '"] .uml-class-name-text tspan:last');-->
<#--var title = document.createElementNS("http://www.w3.org/2000/svg","title");-->
<#--title.innerHTML = $titleEl.text();-->

<#--drawEllipsisIfTextTooLong($titleEl[0], RECT_WIDTH-4);-->

<#--$(el, '.uml-class-name-text').append($(title));-->
<#--});-->

<#--$('.uml-class-attrs-text tspan').each(function(index, el) {-->
<#--var title = document.createElementNS("http://www.w3.org/2000/svg","title");-->
<#--title.innerHTML = $(el).text();-->

<#--drawEllipsisIfTextTooLong(el, RECT_WIDTH-4);-->

<#--$(el).append($(title));-->
<#--});-->

<#--function drawEllipsisIfTextTooLong(textObj, width){-->
<#--var textString = textObj.textContent;-->


<#--if (textObj.getComputedTextLength() > 0)  {-->

<#--//ellipsis is needed-->
<#--if (textObj.getSubStringLength(0, textString.length) >= width) {-->
<#--for (var x = textString.length-1; x > 0; x -= 1){-->
<#--if (textObj.getSubStringLength(0, x+3) <= width){-->
<#--textObj.textContent = textString.substring(0, x) + "...";-->
<#--return;-->
<#--}-->
<#--}-->
<#--textObj.textContent = "..."; //can't place at all-->
<#--}-->
<#--}-->
<#--}-->
<#--</#if>-->

<#--<#macro addVertices package>-->
    <#--<#list package.entityTypes as entityType>-->
        <#--<#if entityType.extends??>-->
        <#--if (classes['<@entityTypeId entityType.extends />']) {-->
        <#--graph.addCell(new uml.Generalization({ source: { id: classes['<@entityTypeId entityType />'].id }, target: { id: classes['<@entityTypeId entityType.extends />'].id }}));-->
        <#--}-->
        <#--</#if>-->
        <#--<#list entityType.attributes as amd>-->
            <#--<#if amd.type == 'xref' || amd.type == 'mref' || amd.type == 'categorical'>-->
            <#--if (classes['<@entityTypeId amd.refEntity />']) {-->
            <#--graph.addCell(new uml.Aggregation({ source: { id: classes['<@entityTypeId entityType />'].id }, target: { id: classes['<@entityTypeId amd.refEntityType />'].id }}));-->
            <#--}-->
            <#--</#if>-->
        <#--</#list>-->
        <#--<#list package.children as p>-->
            <#--<@addVertices package=p />-->
        <#--</#list>-->
    <#--</#list>-->
<#--</#macro>-->

<#--<#macro entityTypeId entityType>${entityType.id?replace("-", "_")?replace(" ", "_")?js_string}</#macro>-->

<#--<#macro listClasses package classes=[]>-->
    <#--<#list package.entityTypes as entityType>-->
        <#--<#if entityType.isAbstract()>-->
        <#--'<@entityTypeId entityType />': new uml.Abstract({-->
        <#--size: { width: RECT_WIDTH, height: ${(50 + 12 * entityType.attributes?size)?c} },-->
        <#--name: '${entityType.label?js_string}',-->
        <#--attrs: {-->
        <#--'.uml-class-attrs-rect': { fill: 'white', stroke: 'black'},-->
        <#--'.uml-class-methods-rect': { fill: 'white', stroke: 'black'},-->
        <#--},-->
        <#--attributes: [<#list entityType.attributes as amd>'${amd.label?js_string}: ${amd.type}'<#if amd_has_next>,</#if></#list>]-->
        <#--}),-->
        <#--<#else>-->
        <#--'<@entityTypeId entityType />': new uml.Class({-->
        <#--size: { width: RECT_WIDTH, height: ${(50 + 12 * entityType.attributes?size)?c} },-->
        <#--name: '${entityType.label?js_string}',-->
        <#--attrs: {-->
        <#--'.uml-class-attrs-rect': { fill: 'white', stroke: 'black'},-->
        <#--'.uml-class-methods-rect': { fill: 'white', stroke: 'black'},-->
        <#--},-->
        <#--attributes: [<#list entityType.attributes as amd>'${amd.label?js_string}: ${amd.type?js_string}'<#if amd_has_next>,</#if></#list>]-->
        <#--}),-->
        <#--</#if>-->
    <#--</#list>-->
    <#--<#list package.children as p>-->
        <#--<@listClasses package=p classes=classes />-->
    <#--</#list>-->
<#--</#macro>-->