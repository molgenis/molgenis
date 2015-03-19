<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css', 'select2.css']>
<#assign js=['single-mapping-project.js', 'bootbox.min.js', 'jquery.scroll.table.body.js', 'select2-patched.js', 'tag-wizard.js']>

<@header css js/>

<div class="row">
	<div class="col-md-12">
		<a onclick="window.history.back()" class="btn btn-default"><span class="glyphicon glyphicon-chevron-left"></span> Back to mapping project</a>	
	</div>
</div>

<div class="row">
	<div class="col-md-6">
		<h3>Tag Wizard</h3>
		<p>Tag attributes manually with ontology term or view and curate auto generated tags</p>
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<div class="control-group">
			<label class="control-label" for="ontology-select">Select an ontology: </label>
			<div class="controls">
				<div class="form-group">
					<div class="col-md-4">
						<select class="form-control " name="ontology-select" id="ontology-select" data-placeholder="Select an ontology to use">
							<option value=""></option>
							<#list ontologies as ontology>
								<option value="${ontology.id}">${ontology.name?html}</option>
							</#list>
						</select>
					</div>
				</div>
				<button type="button" class="btn btn-primary">Tag-o-matic!</button>
				<button type="button" class="btn btn-info">Reannotate</button>
				<button type="button" class="btn btn-danger">Clear tags</button>
			</div>
		</div>
		<hr></hr>
	</div>
</div>

<div class="row">
	<div class="col-md-6" style="overflow-y:auto;max-height:500px;min-height:500px;">
		<#list taggedAttributeMetaDatas?keys as attributeMetaData>
			<h4>${attributeMetaData.name}</h4>
			<p>${attributeMetaData.description}</p>
			
			<table class="table">
				<thead>
					<th>Relation <button type="button" class="btn btn-default btn-xs add-expression-btn"><span class="glyphicon glyphicon-plus"></span></button></th>
					<th>Tags <button type="button" class="btn btn-default btn-xs add-tag-btn"><span class="glyphicon glyphicon-plus"></span></button></th>
				</thead>
				<#if taggedAttributeMetaDatas[attributeMetaData]??>
					<tbody>
					<#list taggedAttributeMetaDatas[attributeMetaData] as tag>
						<tr>
							<td>
								2 be implemented						
							</td>
							<td>
								<button type="btn" class="btn btn-primary btn-xs tag-remove-btn">
									${tag.object.label}<span class="glyphicon glyphicon-remove"></span>
								</button>
							</td>				
						</tr>
					</#list>
					</tbody>
				</#if>
				</table>
			<#if attributeMetaData_has_next><hr></hr></#if>
		</#list>
	</div>
	<div id="relation-tag-info-container" class="col-md-6"></div>
</div>

<script id="relation-and-tag-template" type="text/x-handlebars-template">
	<div class="row">
		<div class="col-md-12">
			<legend>{{this.relation}}</legend>
		</div>
	</div>
	
	<div class="row">
		<div class="col-md-6">
			<select multiple class="form-control" id="tag-dropdown">
				{{#each taglist}}
					<option selected="selected">{{this}}</option>
				{{/each}}
			</select>
		</div>
	</div>
</script>

<@footer/>