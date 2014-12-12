<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['molgenis-form.css', 'workflows.css']>
<#assign js=['jquery.validate.min.js','workflows.js']>

<@header css js/>

<div class="container-fluid">
	<div class="row">
		<div class="col-md-12">
			<legend>
				Workflows <a data-toggle="modal" href="#" data-target="#formModal" style="margin:30px 10px"><img src="/img/new.png"></a>
			</legend>
			
			<form name="search">
	    		<div class="clearfix">
					<div class="col-md-3 pull-right">	
	    				<div class="input-group">
	    					<input id="search" type="search" class="search-query form-control" name="q" placeholder="SEARCH" <#if q??>value="${q!?html}"</#if> >
		            		<span class="input-group-btn">
		            			<button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-search"></span></button>
		            			<button type="submit" id="clearSearchButton" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span></button>
		            		</span>
		            	</div>
	        		</div>
	        	</div>
	        </form>
	        
			<table class="table table-condensed table-bordered">
				<thead>
					<tr>
						<th>Name</th>
						<th>Active</th>
						<th>Description</th>
						<th>Targets</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<#if workflows?has_content>
						<#list workflows as workflow>
							<tr>
								<td>${workflow.name!?html}</td>
								<td><input type="checkbox" <#if workflow.active> checked</#if> disabled /></td>
								<td>${workflow.description!?html}</td>
								<td>${workflow.targetType!?html}</td>
								<td><a href="workflow/${workflow.name?url('UTF-8')}">view</a></td>
							</tr>
						</#list>
					</#if>
				</tbody>
			</table>
		</div>
	</div>
	
	<div class="modal" id="formModal" tabindex="-1" role="dialog" aria-labelledby="formModalTitle" aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">	
				<form class="form-horizontal" id="workflowForm" role="form" action="" method="POST">	 						
	      			<div class="modal-header">
	        			<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        			<h4 class="modal-title" id="formModalTitle">Import workflow</h4>
	     			</div>
	      			<div class="modal-body">
						<div class="row">
							<div class="col-md-12">
								<div class="form-group">
    								<label for="path" class="col-sm-4 control-label">Workflow folder:</label>
    								<div class="col-sm-6">
      									<input type="text" name="path" class="form-control" id="path" placeholder="Enter workflow folder" required>
    								</div>
  								</div>
								<div class="form-group">
    								<label for="workflowFileName" class="col-sm-4 control-label">Workflow filename (.csv):</label>
    								<div class="col-sm-6">
      									<input type="text" name="workflowFileName" class="form-control" id="workflowFileName" placeholder="Enter workflow filename" value="workflow.csv" required>
    								</div>
  								</div>
  								<div class="form-group">
    								<label for="parametersFileName" class="col-sm-4 control-label">Parameters filename (.csv):</label>
    								<div class="col-sm-6">
      									<input type="text" name="parametersFileName" class="form-control" id="parametersFileName" placeholder="Enter parameters filename" value="parameters.csv" required>
    								</div>
  								</div>
							</div>
						</div>
	      			</div>
	      			<div class="modal-footer">
	        			<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        			<button type="button" id="submitFormButton" class="btn btn-primary">Import</button>
	      			</div>
	      		</form>
	    	</div>
		</div>
	</div>
	
	
</div>

<@footer/>