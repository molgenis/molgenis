<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-service.js']>

<@header css js/>
<@createNewMappingProjectModal />

<div class="row">
	<div class="col-md-12">
		<h1>Mapping projects overview</h1>
		<p>Create and view mapping projects</p>
	</div>
</div>

<hr></hr>

<!--Table containing mapping projects-->
<#-- TODO -->
<#-- Generate table with mapping projects dynamicly -->
<#-- Make Project name link to attribute mapping screen  -->
<div class="row">
	<div class="col-md-12">
		<table class="table">
 			<thead>
 				<tr>
 					<th>Mapping name</th>
 					<th>Owner</th>
 					<th>Target entity</th>
 					<th>Mapped sources</th>
 				</tr>
 			</thead>
 			<tbody>
 				<tr>
 					<td><a href="${context_url}/attributemapping" value="HOP-minimal">HOP-minimal project</a></td>
 					<td>Mark</td>
 					<td>HOP-minimal</td>
 					<td>Source1, Source2, Source4</td>
 				</tr>
 				<tr>
 					<td><a href="${context_url}/attributemapping" value="FinRisk">FinRisk project</a></td>
 					<td>Chao</td>
 					<td>FinRisk</td>
 					<td>Source3, Source4</td>
 				</tr>
 			</tbody>
		</table>
	</div>
</div>
	
<div class="row">
	<div class="col-md-12">
		<div class="btn-group" role="group">
			<a href="#" class="btn btn-primary" data-toggle="modal" data-target="#create-new-mapping-project-modal">Create a new mapping</a>  				
		</div>
		<div class="btn-group" role="group">
			<button type="button" class="btn btn-success">Edit</button>  				
		</div>
	</div>
</div>

<@footer/>

<#-- TODO -->
<#-- Generate owner and target entity list dynamicly -->
<#-- Process input to database and update table containing mapping projects -->
<#macro createNewMappingProjectModal>
	<div class="modal fade" id="create-new-mapping-project-modal" tabindex="-1" role="dialog" aria-labelledby="create-new-mapping-project-modal" aria-hidden="true">
		<div class="modal-dialog">
	    	<div class="modal-content">
	        	<div class="modal-header">
	        		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        		<h4 class="modal-title" id="create-new-mapping-project-label">Create a new mapping project</h4>
	        	</div>
	        	<div class="modal-body">
      				<form id ="create-new-mapping-project-form">	
  						<div class="form-group">
		            		<label>Mapping project name</label>
		  					<input type="text" class="form-control" placeholder="Mapping name" required="required">
						</div>
					
						<hr></hr>	
		
						<div class="form-group">
							<label>Select the owner of this project</label>
							<select class="form-control" required="required" placeholder="Select a project owner">
		    					<option value="mark">Mark</option>
		    					<option value="chao">Chao</option>
							</select>
						</div>
						
						<hr></hr>	
						
						<div class="form-group">
							<label>Select the Target entity</label>
							<select class="form-control" required="required" placeholder="Select a target entity">
		    					<option value="hop-minimal">HOP-minimal</option>
		    					<option value="finrisk">FinRisk</option>
							</select>
						</div>
						
						<input type="submit" class="submit" style="display:none;">
					</form>
        		</div>
        		
	        	<div class="modal-footer">
	        		<button type="button" id="submit-new-mapping-project-button" class="btn btn-primary">Create project</button>
	                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	    		</div>	    				
    		</div>
		</div>
	</div>
</#macro>	