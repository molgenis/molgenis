<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['joint.min.css', 'molgenis-form.css', 'workflow.css']>
<#assign js=['lodash.js', 'backbone-min.js', 'geometry.min.js', 'vectorizer.min.js', 'joint.clean.min.js','joint.layout.DirectedGraph.min.js']>

<@header css js/>

<div class="container-fluid">

	<form class="form-horizontal" id="workflowForm" role="form" action="" method="POST">	
		<div class="row">
			<div class="col-md-12">
				<div class="form-group">
    				<label for="name" class="col-md-2 control-label">Workflow name:</label>
    				<div class="col-md-4">
      					<input type="text" name="name" class="form-control" id="name" placeholder="Enter workflow name" value="${workflow.name!}" required>
    				</div>
  				</div>
  				<div class="form-group">
    				<label for="description" class="col-md-2 control-label">Workflow description:</label>
    				<div class="col-md-4">
    					<textarea id="description" name="description" class="form-control" >${workflow.description!}</textarea>
      				</div>
  				</div>
  				<div class="form-group">
    				<label for="target" class="col-md-2 control-label">Workflow targets:</label>
    				<div class="col-md-4">
    					<select id="target" name="target" class="form-control" required>
    						<option value="">Select a target</option>
    						<#list entities as entity>
    							<option <#if workflow.targetType?? && (workflow.targetType['fullName'] == entity.name)>selected</#if> value="${entity.name}">${entity.simpleName}</option>
    						</#list>
    					</select>
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
