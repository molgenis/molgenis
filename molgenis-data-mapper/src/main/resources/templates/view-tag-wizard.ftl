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
		<p>
			Tag attributes manually with ontology terms, or automagically 
			tag attributes with the selected ontology.
		</p>
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<div class="control-group">
			<label class="control-label" for="ontology-select">Select an ontology: </label>
			<div class="controls">
				<div class="form-group">
					<div class="col-md-4">
						<select multiple class="form-control " name="ontology-select" id="ontology-select" data-placeholder="Select an ontology to use">
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
	<div class="col-md-6" style="overflow-y:auto;max-height:500px;min-height:300px;">
		<#list taggedAttributeMetaDatas?keys as attributeMetaData>
			<h4>${attributeMetaData.name}</h4>
			<p>${attributeMetaData.description}</p>
			
			<table class="table" id="${attributeMetaData.name}">
				<thead>
					<th>Relation</th>
					<th>Tags</th>
					<th></th>
				</thead>
					<tbody>
						<tr>
							<td data-relation="Is associated with">Is associated with</td> <#--${tag.relation.iri} ${tag.relation.label}-->
							<td>
								<button type="btn" class="btn btn-primary btn-xs remove-tag-btn">
									Tag1 <span class="glyphicon glyphicon-remove"></span>
								</button>
								<button type="btn" class="btn btn-primary btn-xs remove-tag-btn">
									Tag2 <span class="glyphicon glyphicon-remove"></span>
								</button>
								<button type="btn" class="btn btn-primary btn-xs remove-tag-btn">
									Tag3 <span class="glyphicon glyphicon-remove"></span>
								</button>
							</td>
							<td>
								<button type="btn" class="btn btn-default btn-xs add-new-tags-btn">
									Add new tag <span class="glyphicon glyphicon-plus"></span>
								</button>
							</td>				
						</tr>
					<#if taggedAttributeMetaDatas[attributeMetaData]??>
						<#list taggedAttributeMetaDatas[attributeMetaData] as tag>
							<tr>
								<td>
									<button type="btn" class="btn btn-default btn-xs show-relation-and-tags-btn">
										Expression here...
									</button>						
								</td>
								<td>
									<button type="btn" class="btn btn-primary btn-xs remove-tag-btn">
										${tag.object.label}<span class="glyphicon glyphicon-remove"></span>
									</button>
								</td>				
							</tr>
						</#list>
					</#if>
				</tbody>
			</table>
			<#if attributeMetaData_has_next><hr></hr></#if>
		</#list>
	</div>
	<div id="relation-and-tag-info-container" class="col-md-6"></div>
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