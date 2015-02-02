<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-service.js']>

<@header css js/>
<@createNewSourceColumnModal />

<div class="row">
	<div class="col-md-12">
		<a href="${context_url}" class="btn btn-danger btn-sm">Back to mapping project overview</a>	
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<div class="col-md-6">
			<h1>Mappings for the ${mappingProject.name} project</h1>
			<p>Create and view mappings. Select a target entity and view / edit mappings from other sources</p>
			<hr></hr>
		</div>
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<div class="col-md-6">
			<form class="form-inline">
				<div class="form-group">
					<label>Select the Target entity</label>
					<select id="target-entity-select" name="target-entity" class="form-control" required="required" placeholder="Select a target entity" style="width:200px">
						<#list mappingProject.mappingTargets as target>
							<option value="${target}" <#if target == selectedTarget>selected</#if>>${target.name}</option>
						</#list>
					</select>
				</div>
				<div class="form-group pull-right">
					<a id="add-new-attr-mapping-btn" href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#create-new-source-column-modal">Map new source entity</a>  				
				</div>
			</form>
		</div>
	</div>
</div>

<div class="row">
	<div class="col-md-12" id="target-mapping-table">
		<div class="col-md-6">
			<table class="table">
	 			<thead>
	 				<tr>
	 					<th>Target model: ${selectedTarget}</th>
 					<#list mappingProject.getMappingTarget(selectedTarget).entityMappings as source>
 						<th>Source: ${source.name}</th>
					</#list>
	 				</tr>
	 			</thead>
	 			<tbody>
					<#list mappingProject.getMappingTarget(selectedTarget).target.attributes as attribute>
						<tr>
							<td>
								${attribute.name}
							</td>
							<#list mappingProject.getMappingTarget(selectedTarget).entityMappings as source>
								<td>
									<form method="get" action="${context_url}/editattributemapping">
										<button type="submit" class="btn btn-primary btn-xs">
											<span class="glyphicon glyphicon-pencil"></span>
										</button>
										<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
										<input type="hidden" name="target" value="${selectedTarget}"/>
										<input type="hidden" name="source" value="${source.name}"/>
										<input type="hidden" name="attribute" value="${attribute.name}"/>
										<#if source.getAttributeMapping(attribute.name)??>
											${source.getAttributeMapping(attribute.name).sourceAttributeMetaData.name}
										</#if>
									</form>
								</td>
							</#list>
						</tr>
					</#list>
				</tbody>
			</table>
		</div>
	</div>
</div>

<#macro createNewSourceColumnModal>
	<div class="modal" id="create-new-source-column-modal" tabindex="-1" role="dialog" aria-labelledby="create-new-source-column-modal" aria-hidden="true">
		<div class="modal-dialog">
	    	<div class="modal-content">
	        	<div class="modal-header">
	        		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        		<h4 class="modal-title" id="create-new-source-column-modal-label">Create a new mapping project</h4>
	        	</div>
	        	<div class="modal-body">	
	        		<form id="create-new-source-form" method="post" action="${context_url}/addentitymapping">	
						<div class="form-group">
		            		<label>Select a new source to map against the target attribute</label>
	  						<select name="source" class="form-control" required="required" placeholder="Select a target entity">
		    					<#list entityMetaDatas as entityMetaData>
	    							<option value="${entityMetaData.name?html}">${entityMetaData.name?html}</option>
		    					</#list>
							</select>
						</div>
						
						<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}">
						<input type="hidden" name="target" value="${selectedTarget}">
						<input type="submit" class="submit" style="display:none;">
					</form>
	        	
        		</div>
        		
	        	<div class="modal-footer">
	        		<button type="button" id="submit-new-source-column-btn" class="btn btn-primary">Add source</button>
	                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	    		</div>	    				
    		</div>
		</div>
	</div>
</#macro>	

	