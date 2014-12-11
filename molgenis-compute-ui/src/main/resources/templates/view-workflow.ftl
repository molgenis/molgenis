<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['joint.min.css', 'molgenis-form.css', 'workflow.css']>
<#assign js=['lodash.js', 'backbone-min.js', 'geometry.min.js', 'vectorizer.min.js', 'joint.clean.min.js','joint.layout.DirectedGraph.min.js', 'workflow.js']>

<@header css js/>

<a href="#" id="backButton" class="btn btn-default btn-md"><span class="glyphicon glyphicon-chevron-left"></span> Back to workflows</a>

<div class="container-fluid">

	<form class="form-horizontal" id="workflowForm" role="form" action="" method="POST">	
		<div class="row">
			<div class="col-md-12">
				<div class="form-group">
    				<label for="name" class="col-md-2 control-label">Workflow name:</label>
    				<div class="col-md-4">
      					<input type="text" name="name" class="form-control" id="name" placeholder="Enter workflow name" value="${workflow.name!?html}" required>
    				</div>
  				</div>
  				<div class="form-group">
    				<label for="description" class="col-md-2 control-label">Workflow description:</label>
    				<div class="col-md-4">
    					<textarea id="description" name="description" class="form-control" >${workflow.description!?html}</textarea>
      				</div>
  				</div>
  				<div class="form-group">
    				<label for="targetType" class="col-md-2 control-label">Workflow targets:</label>
    				<div class="col-md-4">
    					<select id="targetType" name="targetType" class="form-control" required>
    						<option value="">Select a target</option>
    						<#list entities as entity>
    							<option <#if workflow.targetType! == entity.name>selected</#if> value="${entity.name!?html}">${entity.simpleName!?html}</option>
    						</#list>
    					</select>
    				</div>
  				</div>
  				<div class="form-group">
    				<div class="col-md-offset-2 col-sm-10">
      					<button type="submit" class="btn btn-default">Save</button>
    				</div>
  				</div>
  			</div>
		</div>					
	</form>
	
	<div id="popover" class="popover right">
      <div class="arrow"></div>
      <div class="popover-content">
      	<p id="popover-content"><p>
      </div>
    </div>
    
	<ul class="nav nav-tabs">
  		<li role="presentation" class="active"><a href="#steps" aria-controls="steps" role="tab" data-toggle="tab">Steps</a></li>
  		<li role="presentation"><a href="#parameters" aria-controls="parameters" role="tab" data-toggle="tab">Parameters</a></li>
	</ul>
	
	<div class="tab-content">
    	<div role="tabpanel" class="tab-pane active" id="steps">
    		<div id="paper"></div>
    	</div>
    	<div role="tabpanel" class="tab-pane col-md-8" id="parameters">
    		<table class="table table-condensed table-bordered">
				<thead>
					<tr>
						<th class="col-md-6">Name</th>
						<th class="col-md-6">Value</th>
					</tr>
				</thead>
				<tbody>
					<#if workflow.parameters?has_content>
						<#list workflow.parameters as parameter>
							<tr>
								<td>${parameter.key!}</td>
								<td>${parameter.value!}</td>
							</tr>
						</#list>
					</#if>
				</tbody>
			</table>	
    	</div>
   </div>
   
</div>

<script>
	<#include "steps.ftl">
</script>

<@footer/>
