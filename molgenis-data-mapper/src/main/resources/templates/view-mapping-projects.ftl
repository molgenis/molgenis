<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-projects.js', 'bootbox.min.js', 'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js']>

<@header css js/>
<@createNewMappingProjectModal />

<!--Table containing mapping projects-->
<div class="row">
	<div class="col-md-6">
		<h1>Mapping projects overview</h1>
		<p>Create and view mapping projects. <#if importerUri??>Upload additional target entities and mapped sources <a href="${importerUri?html}">here</a>.</#if></p>
		
		<#if entityMetaDatas?has_content>
			<div class="btn-group" role="group">
				<button type="button" id="submit-new-source-column-btn" class="btn btn-primary" data-toggle="modal" data-target="#create-new-mapping-project-modal"><span class="glyphicon glyphicon-plus"></span>&nbsp;Add Mapping Project</button>
			</div>
		</#if>	
		<hr/>
	</div>
</div>
<div class="row">
	<div class="col-md-6">
		<#if mappingProjects?has_content>
			<table class="table table-bordered">
	 			<thead>
	 				<tr>
	 					<th></th>
	 					<th>Mapping name</th>
	 					<th>Owner</th>
	 					<th>Target entities</th>
	 					<th>Mapped sources</th>
	 				</tr>
	 			</thead>
	 			<tbody>
	 				<#list mappingProjects as project>
						<#assign broken = false>
						<#if project.mappingTargets[0]??>
							<#list project.mappingTargets[0].entityMappings as mapping>
								<#if !mapping.name??><#assign broken = true></#if> 
							</#list>
						<#else>
							<#assign broken = true>
						</#if>

						<#if broken == false>
							<tr>	
			 					<td>
			 						<#if user==project.owner.username || admin>
				 						<form method="post" action="${context_url}/removeMappingProject" class="pull-left verify">
											<input type="hidden" name="mappingProjectId" value="${project.identifier}"/>
											<button type="submit" class="btn btn-danger btn-xs"><span class="glyphicon glyphicon-trash"></span></button>
										</form>
										<form method="post" action="${context_url}/mappingproject/clone" class="pull-left">
                                            <input type="hidden" name="mappingProjectId" value="${project.identifier?html}"/>
                                            <button type="submit" class="btn btn-default btn-xs clone-btn"><span class="glyphicon glyphicon-duplicate"></span></button>
                                        </form>
									</#if>
			 					</td> 					
			 					<td>
			 						<a href="${context_url}/mappingproject/${project.identifier}">${project.name?html}</a></td>
			 					<td>${project.owner.username?html}</td>
			 					<td>
			 					<#list project.mappingTargets as target>
			 						${target.name?html}<#if target_has_next>, </#if>
		 						</#list>
		 						</td>
			 					<td>
			 					<#list project.mappingTargets[0].entityMappings as mapping>
			 						${mapping.name}<#if mapping_has_next>, </#if> 
		 						</#list>
			 					</td>	
			 				</tr>
						<#else>
							<tr class="danger">
								<td>
			 						<#if user==project.owner.username || admin>
				 						<form method="post" action="${context_url}/removeMappingProject" class="pull-left verify">
											<input type="hidden" name="mappingProjectId" value="${project.identifier}"/>
											<button type="submit" class="btn btn-danger btn-xs"><span class="glyphicon glyphicon-trash"></span></button>
										</form>
									</#if>
			 					</td>
			 					<td>
			 						${project.name?html}
			 					</td>
			 					<td>${project.owner.username?html}</td>
			 					<td colspan="2"><b>Broken project: some entities are missing</b></td>
							</tr>	
						</#if>
	 				
	 				</#list>
	 			</tbody>
			</table>
		</#if>
	</div>
</div>

<@footer/>

<#macro createNewMappingProjectModal>
	<div class="modal fade" id="create-new-mapping-project-modal" tabindex="-1" role="dialog" aria-labelledby="create-new-mapping-project-modal" aria-hidden="true">
		<div class="modal-dialog">
	    	<div class="modal-content">
	        	<div class="modal-header">
	        		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        		<h4 class="modal-title" id="create-new-mapping-project-label">Create a new mapping project</h4>
	        	</div>
	        	<div class="modal-body">
      				<form id="create-new-mapping-project-form" method="post" action="${context_url}/addMappingProject">
  						<div class="form-group">
		            		<label>Mapping project name</label>
		  					<input name="mapping-project-name" type="text" class="form-control" placeholder="Mapping name" required="required">
						</div>
					
						<hr></hr>	
						
						<div class="form-group">
							<label>Select the Target entity</label>
							<select name="target-entity" class="form-control" required="required" placeholder="Select a target entity">
		    					<#list entityMetaDatas as entityMetaData>
		    						<option value="${entityMetaData.name?html}">${entityMetaData.name?html}</option>
		    					</#list>
							</select>
						</div>
					</form>
        		</div>
        		
	        	<div class="modal-footer">
	        		<button type="button" id="submit-new-mapping-project-btn" class="btn btn-primary">Create project</button>
	                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	    		</div>	    				
    		</div>
		</div>
	</div>
</#macro>	