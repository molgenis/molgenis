<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['molgenis-form.css']>
<#assign js=['jquery.validate.min.js']>

<@header css js/>

<div class="container-fluid">
	<div class="row">
		<div class="col-md-12">
			<legend>
				Workflows <a data-toggle="modal" href="#" data-target="#formModal" style="margin:30px 10px"><img src="/img/new.png"></a>
			</legend>
			<table class="table table-condensed table-bordered">
				<thead>
					<tr>
						<th>Name</th>
						<th>Description</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<#if workflows?has_content>
						<#list workflows as workflow>
							<tr>
								<td>${workflow.name}</td>
								<td>${workflow.description!}</td>
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
				<form class="form-horizontal" role="form" action="" method="POST">	 						
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
	        			<button type="submit" id="submitFormButton" class="btn btn-primary">Import</button>
	      			</div>
	      		</form>
	    	</div>
		</div>
	</div>
	
	
</div>

<@footer/>