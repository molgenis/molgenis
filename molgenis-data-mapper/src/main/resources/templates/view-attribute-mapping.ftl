<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-service.js','d3.min.js','vega.min.js','jstat.min.js', 'biobankconnect-graph.js', 'jquery.scroll.table.body.js', 'bootbox.min.js']>

<@header css js/>

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
				<input type="hidden" name="target" value="${entityMapping.targetEntityMetaData.name?html}"/>
				<input type="hidden" name="source" value="${entityMapping.name?html}"/>
				<input type="hidden" name="targetAttribute" value="${attributeMapping.targetAttributeMetaData.name?html}"/>
				<textarea class="form-control" name="algorithm" id="edit-algorithm-textarea"></textarea>
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
								<td>${source.label?html}</td>
								<td>
								<#if source.description??>
									${source.description?html}
								</#if>
								</td>
								<td>0</td>
								<td>
									<input required type="radio" name="sourceAttribute" value="${source.name}"
										<#if source.name == selected> checked="checked"</#if>
										<#if !hasWritePermission> disabled="disabled"</#if> />
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
				<#if hasWritePermission>
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
			
		</div>
		<hr />
		<button type="submit" class="btn btn-primary" id="btn-test">Test</button>
	</div>
</div>
<div id="statistics-container" class="row">
	<div class="col-md-12">
		<div class="row">
			<div class="col-md-6">
				<center><legend>Summary statistics</legend></center>
				<table class="table table-bordered">
					<tr><th>Total cases</th><td id="stats-total"></td></tr>
					<tr><th>Valid cases</th><td id="stats-valid"></td></tr>
					<#switch attributeMapping.targetAttributeMetaData.dataType>
  						<#case "long">
  						<#case "decimal">
  						<#case "int">
						<tr><th>Mean</th><td id="stats-mean"></td></tr>
						<tr><th>Median</th><td id="stats-median"></td></tr>
						<tr><th>Standard deviation</th><td id="stats-stdev"></td></tr>
					</#switch>
				</table>
			</div>
			<div class="col-md-6">
				<center><legend>Distribution plot</legend></center>
				<div class="distribution">
				</div>
			</div>
		</div>
	</div>
</div>

<script>
	var editor = ace.edit("edit-algorithm-editor");
	var textarea = $("#edit-algorithm-textarea").hide();
	$('#statistics-container').hide();
	
	var showStatistics = function(data){
		if(data.results.length > 0) {
			$('#stats-total').text(data.totalCount);
			$('#stats-valid').text(data.results.length);
			$('#stats-mean').text(jStat.mean(data.results));
			$('#stats-median').text(jStat.median(data.results));
			$('#stats-stdev').text(jStat.stdev(data.results));
			
			$('#statistics-container').show();
			$('.distribution').bcgraph(data.results);
		} else {
			$('#statistics-container').hide();
			molgenis.createAlert([{'message':'There are no values generated for this algorithm'}],'error');
		}
	};
		
	$('#attribute-mapping-table').scrollTableBody();
	editor.setOptions({
		enableBasicAutocompletion: true
	});
	editor.setTheme("ace/theme/eclipse");
	editor.getSession().setMode("ace/mode/javascript");
	<#if !hasWritePermission>editor.setReadOnly(true);</#if>
	editor.getSession().on('change', function(){
		textarea.val(editor.getSession().getValue());
	});
	editor.setValue("${(attributeMapping.algorithm!"")?html}");
	
	var updateEditor = function(){
		editor.setValue("$('"+$(this).val()+"')");
	}
	
	$('input[name="sourceAttribute"]').change(updateEditor);
	$('#attribute-table-container form').on('reset', function() {
		editor.setValue("${(attributeMapping.algorithm!"")?html}");
	});
	
	$('#btn-test').click(function(){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/mappingattribute/testscript',
			async : false,
			data : JSON.stringify({
				targetEntityName : '${entityMapping.targetEntityMetaData.name?js_string}', 
				sourceEntityName : '${entityMapping.name?js_string}', 
				targetAttributeName : '${attributeMapping.targetAttributeMetaData.name?js_string}',
				algorithm: editor.getValue()
			}),
			contentType : 'application/json',
			success : showStatistics
		});
	});

</script>
<@footer/>