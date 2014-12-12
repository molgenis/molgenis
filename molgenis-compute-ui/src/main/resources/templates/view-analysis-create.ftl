<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["analysis.css"]>
<#assign js=["analysis.js"]>

<@header css js/>
<#if message??>
	<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>SUCCESS!</strong> ${message?html}</div>	
</#if>
<div class="row">
	<div class="col-md-offset-2 col-md-8">
<div class="row">
	<div class="col-md-12">
		<form class="form-horizontal" role="form" action="${context_url?html}" method="GET">
			<button class="btn btn-default" type="submit"><span class="glyphicon glyphicon-chevron-left"></span> Back to analysis</button>
		</form>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<form class="form-horizontal" role="form" action="${context_url?html}" method="POST">
			<div class="form-group">
				<label class="col-md-4 control-label" for="name">Analysis Name *</label>
				<div class="col-md-5">
                    <input id="analysis-workflow-name"  type="text" class="form-control" name="name" value="${analysis.name?html}" placeholder="Choose name..." requireddata-id="/api/v1/computeui_Analysis/${analysis.identifier?html}/name" >
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-4 control-label" for="description">Analysis Description</label>
				<div class="col-md-5">
                    <input id="analysis-workflow-description" type="text" class="form-control" name="description" value="${analysis.description!?html}" data-id="/api/v1/computeui_Analysis/${analysis.identifier?html}/description">
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-4 control-label" for="country">Analysis Workflow *</label>
				<div class="col-md-4">
					<div class="row">
						<div class="col-md-6">
							<select id="analysis-workflow-select" class="form-control" name="workflow"<#if targetId??> disabled</#if> data-id="/api/v1/computeui_Analysis/${analysis.identifier?html}/workflow">
								<#list workflows.iterator() as workflow>
								<option value="${workflow.identifier?html}"<#if workflow.identifier == analysis.workflow.identifier> selected</#if>>${workflow.name?html}</option>
								</#list>
							</select>
						</div>
						<div class="col-md-6">
							<#-- TODO implement -->
							<a href="#" id="view-workflow-btn">view</a>	
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<ul class="nav nav-tabs" role="tablist">
		   <li role="presentation" class="active"><a href="#target" aria-controls="target" role="tab" data-toggle="tab">Target</a></li>
		</ul>
		<div class="tab-content">
    		<div role="tabpanel" class="tab-pane active" id="target">
				<#-- TODO implement -->
				<span>TODO implement</span>
    		</div>
    	</div>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<form name="execute-workflow-form" class="form-horizontal" action="${context_url?html}/execute" method="POST" data-id="/api/v1/computeui_Analysis/${analysis.identifier?html}">
			<input type="hidden" name="workflowId" value="${analysis.workflow.identifier?html}">
			<input type="hidden" name="targetId" value="${targetId!"not specified"?html}">
			<button id="delete-analysis-btn" "type="button" class="btn btn-default">Delete</button>
			<button type="button" class="btn btn-default">Clone</button>	
			<button type="submit" class="btn btn-default">Run</button>	
		</form>
	</div>
</div>
</div>
</div>
<@footer/>