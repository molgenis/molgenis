<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-service.js','d3.min.js','vega.min.js','jstat.min.js', 'biobankconnect-graph.js', 'jquery.scroll.table.body.js']>

<@header css js/>

<#--TODO why not in js assign?-->
<script src="<@resource_href "/js/ace/src-min-noconflict/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace/src-min-noconflict/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>

<#if attributeMapping.sourceAttributeMetaData??>
	<#assign selected=attributeMapping.sourceAttributeMetaData.name>
<#else>
	<#assign selected="null">
</#if>
<div class="row">
	<div class="col-md-12">
		<h4>Mapping to <i>${entityMapping.targetEntityMetaData.name}.${attributeMapping.targetAttributeMetaData.name}</i> from <i>${entityMapping.sourceEntityMetaData.name}</i></h4>
		<hr />
	</div>
</div>
<div class="row">

	<div class="col-md-6">
		<h5>Source attributes</h5>
		<div id="attribute-table-container" >
			<form method="POST" action="${context_url}/saveattributemapping">
				<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
				<input type="hidden" name="target" value="${entityMapping.targetEntityMetaData.name}"/>
				<input type="hidden" name="source" value="${entityMapping.name}"/>
				<input type="hidden" name="targetAttribute" value="${attributeMapping.targetAttributeMetaData.name}"/>
				<table id="attribute-mapping-table" class="table table-bordered">
					<thead>
						<tr>
							<th>Name</th>
							<th>Description</th>
							<th>Score</th>
							<th>Select</th>
						</tr>
					</thead>
					<tbody>
						<#list entityMapping.sourceEntityMetaData.attributes as source>
							<tr>
								<td>${source.name}</td>
								<td>
								<#if source.description??>
									${source.description}
								</#if>
								</td>
								<td>0</td>
								<td>
									<input type="radio" name="sourceAttribute" value="${source.name}"
										<#if source.name == selected> checked="checked"</#if>
										<#if !mayChange> disabled="disabled"</#if> />
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
				<#if mayChange>
					<button type="submit" class="btn btn-primary">Save</button> 
					<button type="reset" class="btn btn-danger">Reset</button>
			        <button type="button" class="btn btn-default" onclick="window.history.back()">Cancel</button>
			    <#else>
			    	<button type="button" class="btn btn-primary" onclick="window.history.back()">Back</button>
		        </#if>
			</form>
		</div>
	</div>
	<div class="col-md-6">
		<h5>Algorithm</h5>
		<div id="edit-algorithm-editor" style="width: 100%; height:380px" class="uneditable-input">
			<textarea class="form-control" name="edit-algorithm-textarea" id="edit-algorithm-container"></textarea>
		</div>
		
		<hr />
		
		<button type="submit" class="btn btn-primary" id="btn-test">Test</button>
	</div>
</div>
<div class="row">
	<div id="statistics-container" class="col-md-12"></div>
</div>

<script>
	var $statisticsContainer = $('#statistics-container');
	var editor = ace.edit("edit-algorithm-editor");
	$('#attribute-mapping-table').scrollTableBody();
	editor.setOptions({
		enableBasicAutocompletion: true
	});
	editor.setTheme("ace/theme/eclipse");
	editor.getSession().setMode("ace/mode/javascript");
		
	var textarea = $("edit-algorithm-textarea").hide();
	<#if !mayChange>editor.setReadOnly(true);</#if>
	editor.getSession().on('change', function(){
		textarea.val(editor.getSession().getValue());
	});
	editor.setValue("${(attributeMapping.algorithm!"")?html}");
	
	$('input[name="sourceAttribute"]').click(function(){
		editor.setValue("$('"+$(this).val()+"')");
	});
	
	$('#btn-test').click(function(){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/mappingattribute/testscript',
			async : false,
			data : JSON.stringify({
				targetEntityName : '${entityMapping.targetEntityMetaData.name}', 
				sourceEntityName : '${entityMapping.name}', 
				targetAttributeName : '${attributeMapping.targetAttributeMetaData.name}',
				algorithm: editor.getValue()
			}),
			contentType : 'application/json',
			success : function(data) {
				$statisticsContainer.empty();
				if(data.results.length > 0){
					var layout = $('<div class="row"></div>').appendTo($statisticsContainer);
					$('<div class="col-md-6"></div>').append('<center><legend>Summary statistics</legend></center>').append(statisticsTable(data.results, data.totalCount)).appendTo(layout);
					$('<div class="col-md-6"></div>').append('<center><legend>Distribution plot</legend></center>').bcgraph(data.results).appendTo(layout);
				} else {
					molgenis.createAlert([{'message':'There are no values generated for this algorithm'}],'error');
				}
			}
		});
	});
	
	function statisticsTable(dataset, totalCount){
			var table = $('<table />').addClass('table table-bordered');
			table.append('<tr><th>Total cases</th><td>' + totalCount + '</td></tr>');
			table.append('<tr><th>Valid cases</th><td>' + dataset.length + '</td></tr>');
			table.append('<tr><th>Mean</th><td>' + jStat.mean(dataset) + '</td></tr>');
			table.append('<tr><th>Median</th><td>' + jStat.median(dataset) + '</td></tr>');
			table.append('<tr><th>Standard Deviation</th><td>' + jStat.stdev(dataset) + '</td></tr>');
			return table;
		}

</script>

<#--<script>
	$(function(){
		
		var attributeMappingRequest = {'targetEntityName' : 'HOP', 'sourceEntityName' : 'FinRisk', 'targetAttributeName' : 'HOP_HEIGHT'};
		
		createAttributeTable($('#attribute-table-container'), attributeMappingRequest);
		
		createEditor($('#edit-algorithm-container'), $('#statistics-container'), attributeMappingRequest);
		
		function createEditor(container, statisticsContainer, attributeMappingRequest){
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
						statisticsContainer.empty();
						if(data.results.length > 0){
							var layout = $('<div class="row"></div>').appendTo(statisticsContainer);
							$('<div class="col-md-6"></div>').append('<center><legend>Summary statistics</legend></center>').append(statisticsTable(data.results, data.totalCount)).appendTo(layout);
							$('<div class="col-md-6"></div>').append('<center><legend>Distribution plot</legend></center>').bcgraph(data.results).appendTo(layout);
						}else{
							molgenis.createAlert([{'message':'There are no values generated for this algorithm'}],'error');
						}
					}
				});
			});
		}
		
		function statisticsTable(dataset, totalCount){
			var table = $('<table />').addClass('table table-bordered');
			table.append('<tr><th>Total cases</th><td>' + totalCount + '</td></tr>');
			table.append('<tr><th>Valid cases</th><td>' + dataset.length + '</td></tr>');
			table.append('<tr><th>Mean</th><td>' + jStat.mean(dataset) + '</td></tr>');
			table.append('<tr><th>Median</th><td>' + jStat.median(dataset) + '</td></tr>');
			table.append('<tr><th>Standard Deviation</th><td>' + jStat.stdev(dataset) + '</td></tr>');
			return table;
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
-->
<@footer/>