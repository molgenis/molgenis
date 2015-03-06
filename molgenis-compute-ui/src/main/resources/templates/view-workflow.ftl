<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['joint.min.css', 'molgenis-form.css', 'select2.css', 'workflow.css']>
<#assign js=['lodash.js', 'backbone-min.js', 'geometry.min.js', 'vectorizer.min.js', 'joint.clean.min.js','joint.layout.DirectedGraph.min.js', 'ace/src-min-noconflict/ace.js', 'select2-patched.js', 'workflow.js']>

<@header css js/>

<div class="container-fluid">
	
	<form class="form-horizontal" id="workflowForm" role="form" action="" method="POST">	
		<div class="row">
			<div class="col-md-2">
				<a href="#" id="backButton" class="btn btn-default btn-md"><span class="glyphicon glyphicon-chevron-left"></span> Back to workflows</a>
			</div>
			<div class="col-md-10">
				<div class="form-group">
    				<label for="name" class="col-md-2 control-label">Name:</label>
    				<div class="col-md-4">
      					<input type="text" name="name" class="form-control" id="name" placeholder="Enter workflow name" value="${workflow.name!?html}" required>
    				</div>
  				</div>
  				<div class="form-group">
    				<label for="description" class="col-md-2 control-label">Description:</label>
    				<div class="col-md-4">
    					<textarea id="description" name="description" class="form-control" >${workflow.description!?html}</textarea>
      				</div>
  				</div>
  				<div class="form-group">
    				<label for="targetType" class="col-md-2 control-label">Targets:</label>
    				<div class="col-md-4">
    					<select id="targetType" name="targetType" class="form-control" placeholder="Select target type" required>
    						<option value=""></option>
    						<#list entities as entity>
    							<option <#if workflow.targetType! == entity.name>selected</#if> value="${entity.name!?html}">${entity.simpleName!?html}</option>
    						</#list>
    					</select>
    				</div>
  				</div>
  				<div class="form-group">
    				<label for="active" class="col-md-2 control-label">Active:</label>
    				<div class="col-md-4">
    					<input type="checkbox" id="active" name="active" <#if workflow.active> checked</#if> />
      				</div>
  				</div>
  			</div>
		</div>					
	</form>
	
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

<div class="modal" id="protocolModal" tabindex="-1" role="dialog" aria-labelledby="formModalTitle" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">	
			<div class="modal-header">
	       		<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        	<h4 class="modal-title" id="formModalTitle"></h4>
	     	</div>
	      	<div class="modal-body">
	      		<label for="parameterMapping">Parameter mapping</label>
	      		<div id="parameterMappingHolder">
	      			<input type="text" id="parameterMapping" disabled />
	      		</div>
	      		
				<label for="protocolTemplate">Protocol</label>
				<div id="protocolTemplate"></div>
			</div>
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
	      	</div>
	    </div>
	</div>
</div>
	
<script>
	<#include "steps.ftl">
</script>

<@footer/>
