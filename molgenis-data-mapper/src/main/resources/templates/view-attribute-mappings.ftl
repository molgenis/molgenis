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
			<h1>Mappings for the ${mappingProject.getIdentifier()} project</h1>
			<p>Create and view mappings. Select a target entity and view / edit mappings from other sources</p>
			<hr></hr>
		</div>
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<div class="col-md-6">
			<div class="btn-group" role="group">
				<a id="add-new-attr-mapping-btn" href="#" class="btn btn-primary" data-toggle="modal" data-target="#create-new-source-column-modal">Map new source entity</a>  				
			</div>
		</div>
	</div>
</div>

<div class="row">
	<div id="attribute-mapping-table-container" class="col-md-12">
		<#--TODO-->
		<#--For every new source, create a div and table component-->
		<div class="col-md-3">
			<table class="table">
				<thead>
					<tr>
						<th>Target model: <#--${targetEntity}--></th>
					</tr>
				</thead>
				<tbody>
					<#--TODO-->
					<#--Generate list of target model attributes-->			
				</tbody>
			</table>
		</div>
	</div>
</div>

<#macro createNewSourceColumnModal>
	<div class="modal fade" id="create-new-source-column-modal" tabindex="-1" role="dialog" aria-labelledby="create-new-source-column-modal" aria-hidden="true">
		<div class="modal-dialog">
	    	<div class="modal-content">
	        	<div class="modal-header">
	        		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        		<h4 class="modal-title" id="create-new-source-column-modal-label">Create a new mapping project</h4>
	        	</div>
	        	<div class="modal-body">	
					<div class="form-group">
	            		<label>Select a new source to map against the target attribute</label>
  						<select id="new-source-entity" class="form-control" required="required" placeholder="Select a target entity">
	    					<option value="hop-minimal">HOP-minimal</option>
	    					<option value="finrisk">FinRisk</option>
						</select>
					</div>
        		</div>
        		
	        	<div class="modal-footer">
	        		<button type="button" id="submit-new-source-column-btn" class="btn btn-primary">Create project</button>
	                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	    		</div>	    				
    		</div>
		</div>
	</div>
</#macro>	

	