<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['attribute-mapping.js', 'd3.min.js','vega.min.js','jstat.min.js', 'biobankconnect-graph.js', 'jquery.scroll.table.body.js', 'bootbox.min.js', 'jquery.ace.js']>

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
		<h4>Mapping from <i>${entityMapping.sourceEntityMetaData.name}</i> to <i>${entityMapping.targetEntityMetaData.name?html}.${attributeMapping.targetAttributeMetaData.label?html}</i>.</h4>
		${(attributeMapping.targetAttributeMetaData.description!"")?html}
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
				<table id="attribute-mapping-table" class="table table-bordered scroll">
					<thead>
						<tr>
							<th>Name</th>
							<th>Description</th>
							<th>Selected</th>
							<th>Insert</th>
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
								<td>
									<input type="checkbox" name="${source.name}" disabled="disabled"/>
								</td>
								<td>
									<button class="btn btn-default insert" data-attribute="${source.name}"><span class="glyphicon glyphicon-chevron-right"></span></button>
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
		</div>
	</div>
	<div class="col-md-6">
		<h5>Algorithm</h5>
		<textarea class="form-control" name="algorithm" rows="15"
			id="edit-algorithm-textarea" <#if !hasWritePermission>data-readOnly="true"</#if>></textarea>
		<hr />
		<button class="btn btn-primary" id="btn-test">Test</button>
	</div>
	</form>
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
<@footer/>