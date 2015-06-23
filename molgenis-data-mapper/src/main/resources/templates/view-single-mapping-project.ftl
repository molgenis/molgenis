<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['single-mapping-project.js', 'bootbox.min.js', 'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js']>

<@header css js/>

<script src="<@resource_href "/js/ace/src-min-noconflict/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace/src-min-noconflict/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>

<div class="row">
	<div class="col-md-12">
		<a href="${context_url}" class="btn btn-default btn-xs">
			<span class="glyphicon glyphicon-chevron-left"></span> Back to mapping project overview
		</a>
		<hr></hr>	
	</div>
</div>

<div class="row">
	<div class="col-md-6">
		<h3>Mappings for the ${mappingProject.name?html} project</h3>
		<p>Create and view mappings.</p>
	</div>
</div>

<!--Table for Target and Source attribute metadata-->
<div class="row">
	<div class="col-md-10">
		<table id="attribute-mapping-table" class="table table-bordered">
 			<thead>
 				<tr>
 					<th>Target model: ${selectedTarget?html}</th>
					<#list mappingProject.getMappingTarget(selectedTarget).entityMappings as source>
						<th>Source: ${source.name?html}
							<#if hasWritePermission>
								<div class="pull-right">
									<form method="post" action="${context_url}/removeEntityMapping" class="verify">
										<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
										<input type="hidden" name="target" value="${selectedTarget}"/>
										<input type="hidden" name="source" value="${source.name}"/>
										<button type="submit" class="btn btn-danger btn-xs pull-right"><span class="glyphicon glyphicon-trash"></span></button>
									</form>
								</div>
							</#if>	
						</th>
					</#list>
 				</tr>
 			</thead>
 			<tbody>
				<#list mappingProject.getMappingTarget(selectedTarget).target.getAtomicAttributes().iterator() as attribute>
					<#if !attribute.isIdAtrribute()>
						<tr>
							<td>
								<b>${attribute.label?html}</b> (${attribute.dataType})
								<#if !attribute.nillable> <span class="label label-default">required</span></#if>
								<#if attribute.unique> <span class="label label-default">unique</span></#if>
								<#if attribute.description??><br />${attribute.description?html}</#if>
								<#if attribute.tags??><br />${attribute.tags?html}</#if>
								<#if attributeTagMap[attribute.name]??>
									<br />
									<#list attributeTagMap[attribute.name] as tag>
										<span class="label label-danger"> ${tag.label}</span>
									</#list>
								</#if>
							</td>
							<#list mappingProject.getMappingTarget(selectedTarget).entityMappings as source>
								<td>
									<div class="pull-right">
										<form method="get" action="${context_url}/attributeMapping" class="pull-right">
											<button type="submit" class="btn btn-default btn-xs">
												<span class="glyphicon glyphicon-pencil"></span>
											</button>
											<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
											<input type="hidden" name="target" value="${selectedTarget}"/>
											<input type="hidden" name="source" value="${source.name}"/>
											<input type="hidden" name="targetAttribute" value="${attribute.name}"/>
										</form>
										<#if hasWritePermission && source.getAttributeMapping(attribute.name)??>
											<form method="post" action="${context_url}/removeAttributeMapping" class="pull-right verify">
												<button type="submit" class="btn btn-default btn-xs">
													<span class="glyphicon glyphicon-remove"></span>
												</button>
												<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
												<input type="hidden" name="target" value="${selectedTarget}"/>
												<input type="hidden" name="source" value="${source.name}"/>
												<input type="hidden" name="attribute" value="${attribute.name}"/>
											</form>
										</#if>
									</div>
									<div>
										<#if source.getAttributeMapping(attribute.name)??>
											<#list source.getAttributeMapping(attribute.name).sourceAttributeMetaDatas as mappedSourceAttribute>
												${mappedSourceAttribute.label?html}<#if mappedSourceAttribute_has_next>, </#if>
											</#list>
										<#elseif !attribute.nillable>
											<span class="label label-danger">missing</span>
										</#if>
									</div>
								</td>
							</#list>
						</tr>
					</#if>
				</#list>
			</tbody>
		</table>
		
	</div>
	<#if entityMetaDatas?has_content && hasWritePermission>
		<div class="col-md-2">
			<a id="add-new-attr-mapping-btn" href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#create-new-source-column-modal"><span class="glyphicon glyphicon-plus"></span>Add source</a>
		</div>
	</#if>
</div>

<div class="row">
	<div class="col-md-2">
		<form method="get" action="${context_url}/tagwizard">
			<input type="hidden" name="target" value="${selectedTarget}"/>
			<div class="btn-group" role="group">
				<button type="submit" class="btn btn-primary">
					<span class="glyphicon glyphicon-tag"></span> Run tag wizard
				</button>
			</div>
		</form>
	</div>	
		
	<#if mappingProject.getMappingTarget(selectedTarget).entityMappings?has_content>
		<div class="col-md-8">		
			<a id="add-new-attr-mapping-btn" href="#" class="btn btn-success pull-right" data-toggle="modal" data-target="#create-integrated-entity-modal">
				<span class="glyphicon glyphicon-play"></span> Create integrated dataset
			</a>
		</div>
	</#if>
</div>

<!--Create new source dialog-->
<div class="modal" id="create-new-source-column-modal" tabindex="-1" role="dialog">
	<div class="modal-dialog">
    	<div class="modal-content">
        	<div class="modal-header">
        		<button type="button" class="close" data-dismiss="modal">&times;</button>
        		<h4 class="modal-title" id="create-new-source-column-modal-label">Add new source</h4>
        	</div>
        	<div class="modal-body">	
        		<form id="create-new-source-form" method="post" action="${context_url}/addEntityMapping">	
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
				</form>
    		</div>
    		
        	<div class="modal-footer">
        		<button type="button" id="submit-new-source-column-btn" class="btn btn-primary">Add source</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
    		</div>	    				
		</div>
	</div>
</div>

<!--Create integrated entity dialog-->
<div class="modal" id="create-integrated-entity-modal" tabindex="-1" role="dialog">
	<div class="modal-dialog">
    	<div class="modal-content">
        	<div class="modal-header">
        		<button type="button" class="close" data-dismiss="modal">&times;</button>
        		<h4 class="modal-title" id="create-integrated-entity-modal-label">Create integrated dataset</h4>
        	</div>
        	<div class="modal-body">
        		<form id="create-integrated-entity-form" method="post" action="${context_url}/createIntegratedEntity">
        			
        			<label>Enter a name for the integrated dataset</label>
        			<input name="newEntityName" type="text" value="" required></input>
        		
        			<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}">
        			<input type="hidden" name="target" value="${selectedTarget}">
				</form>
    		</div>
        	<div class="modal-footer">
        		<button type="button" id="create-integrated-entity-btn" class="btn btn-primary">Create integrated dataset</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
    		</div>	    				
		</div>
	</div>
</div>
<@footer/>