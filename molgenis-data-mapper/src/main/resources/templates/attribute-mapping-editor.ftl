<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-service.js','d3.min.js','vega.min.js','jstat.min.js', 'biobankconnect-graph.js']>

<@header css js/>

<script src="<@resource_href "/js/ace/src-min-noconflict/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace/src-min-noconflict/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>

<div class="row">
	<div class="col-md-12">
		<h3>Mappping to <u>HOP_HEIGHT</u> from <u>FinRisk</u></h3>
	</div></br></br></br>
</div>
<div class="row">
	<div id="attribut-table-container" class="col-md-6"></div>
	<div id="edit-algorithm-container" class="col-md-6"></div>
</div>
<script>
	$(function(){
		
		var attributeMappingRequest = {'targetEntityIdentifier' : 'HOP', 'sourceEntityIdentifier' : 'FinRisk', 'targetAttributeIdentifier' : 'HOP_HEIGHT'};
		
		createAttributeTable($('#attribut-table-container'), attributeMappingRequest);
		
		createEditor($('#edit-algorithm-container'), attributeMappingRequest);
		
		function createEditor(container, attributeMappingRequest){
			var button = $('<button class="btn btn-primary" type="button">Test</button></br>').css({'margin-bottom':'10px'}).appendTo(container);
			var algorithmEditorDiv = $('<div id="algorithmEditorDiv"></div>').addClass('well').css('height', $(document).height()/4).appendTo(container);
			var langTools = ace.require("ace/ext/language_tools");
			var editor = ace.edit('algorithmEditorDiv');
			editor.getSession().setMode("ace/mode/javascript");
			editor.setOptions({
			    enableBasicAutocompletion: true
			});
			button.click(function(){
				$.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + '/mappingattribute/testscript',
					async : false,
					data : JSON.stringify($.extend(attributeMappingRequest, {'algorithm' : editor.getValue()})),
					contentType : 'application/json',
					success : function(data) {
						var graphDiv = $('<div />').attr('id', 'graph-id').addClass('col-md-6 well').append('<div class="legend-align-center">Distribution plot</div>').bcgraph(data.result);
						container.append(graphDiv);
					}
				});
			});
		}
			
		function createAttributeTable(container, attributeMappingRequest){
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/mappingattribute/',
				async : false,
				data : JSON.stringify(attributeMappingRequest),
				contentType : 'application/json',
				success : function(data) {
					if(data.sourceAttributes !== null && data.sourceAttributes.length > 0){
						var table = $('<table />').addClass('table').append('<tr><th>Name</th><th>Description</th><th>Score</th><th>Select</th></tr>');
						$.each(data.sourceAttributes, function(i, attribute){
							table.append('<tr><td>' + attribute.name + '</td><td>' + attribute.label + '</td><td></td><td><input type="checkbox"/></td></tr>');
						});
						container.append('Candidate attributes sorted by matching score </br></br>').append(table);
					}else{
						container.append('There are no attributes found in entity : <strong>' + attributeMappingRequest.sourceEntityIdentifier + '</strong>');
					}
				}
			});
		}
	});
</script>
<@footer/>