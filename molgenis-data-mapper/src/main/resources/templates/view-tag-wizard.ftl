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
							<#if selectedOntologies??>
								<#list selectedOntologies as selectedOntology>
									<#if selectedOntology??>
										<option selected='selected' data-iri="${selectedOntology.IRI} "value="${selectedOntology.id}">${selectedOntology.name?html}</option>
									</#if>
								</#list>
							</#if>
							<#list ontologies as ontology>
								<option data-iri="${ontology.IRI}" value="${ontology.id}">${ontology.name?html}</option>
							</#list>
						</select>
					</div>
				</div>
				<button type="button" class="btn btn-primary">Tag-o-matic!</button>
				<button type="button" class="btn btn-info">Re-annotate</button>
				<button type="button" class="btn btn-danger">Clear tags</button>
			</div>
		</div>
		<hr></hr>
	</div>
</div>

<div class="row">
	<div class="col-md-6" style="overflow-y:auto;max-height:500px;min-height:300px;">
		<#list attributes as attributeMetaData>
			<h4>${attributeMetaData.name}</h4>
			<p>${attributeMetaData.description!""}
			
			<table class="table" id="${attributeMetaData.name}">
				<thead>
					<th>Relation</th>
					<th>Tags</th>
					<th></th>
				</thead>
					<tbody>
					<#assign relationsAndTagsMap = taggedAttributeMetaDatas[attributeMetaData.name]>
					<#if relationsAndTagsMap.keySet()?has_content>
						<#list relationsAndTagsMap.keySet() as relation>
							<tr>
								<td data-relation="${relation.IRI}">${relation.label}</td>
								<td>
									<#list relationsAndTagsMap.get(relation) as tag>
										<button type="btn" class="btn btn-primary btn-xs remove-tag-btn" data-relation="${relation.IRI}" 
											data-entity="${entity.name}" data-attribute="${attributeMetaData.name}" data-tag="${tag.IRI}">
												${tag.label} <span class="glyphicon glyphicon-remove"></span>
										</button>
									</#list>
								</td>
								<td>
								<button type="btn" class="btn btn-default btn-xs edit-attribute-tags-btn" data-relation="${relation.IRI}" 
									data-attribute="${attributeMetaData.name}" data-toggle="modal" data-target="#edit-ontology-modal">
										Edit <span class="glyphicon glyphicon-pencil"></span>
								</button>
							</td>
							</tr>
						</#list>
					<#else>
						<tr>
						<td data-relation="http://iri.org/#isAssociatedWith">Is associated with</td>
							<td></td>
							<td>
								<button type="btn" class="btn btn-default btn-xs edit-attribute-tags-btn" data-relation="http://iri.org/#isAssociatedWith" 
									data-attribute="${attributeMetaData.name}" data-toggle="modal" data-target="#edit-ontology-modal">
										Edit <span class="glyphicon glyphicon-pencil"></span>
								</button>
							</td>
						</tr>
					</#if>
				</tbody>
			</table>
			<#if attributeMetaData_has_next><hr></hr></#if>
		</#list>
	</div>
	<div id="edit-ontology-container" class="col-md-6"></div>
</div>

<div class="modal fade" id="edit-ontology-modal">
	<div class="modal-dialog">
		<div class="modal-content">
			
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
				<h4 class="modal-title">Select ontologies to add as tags</h4>
			</div>
			
			<div class="modal-body">
				<div class="row">
					<div class="col-md-12">
						<input id="tag-dropdown" type="hidden"></input>
					</div>
				</div>
			</div>
			
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<button id="save-tag-selection-btn" data-entity="${entity.name}" type="button" class="btn btn-primary">Save changes</button>
			</div>
		</div>
	</div>
</div>

<@footer/>