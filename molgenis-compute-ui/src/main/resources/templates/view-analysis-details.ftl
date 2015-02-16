<div class="row">
	<div class="col-md-12">
		<button class="btn btn-default" type="button" id="analysis-back-btn"><span class="glyphicon glyphicon-chevron-left"></span> Back to analysis</button>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<form class="form-horizontal" role="form" action="${context_url?html}" method="POST">
			<div class="form-group">
				<label class="col-md-4 control-label" for="name">Analysis Name *</label>
				<div class="col-md-5">
                    <input id="analysis-name"  type="text" class="form-control" name="name" value="" placeholder="Choose name..." required>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-4 control-label" for="description">Analysis Description</label>
				<div class="col-md-5">
                    <input id="analysis-description" type="text" class="form-control" name="description" value="">
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-4 control-label" for="country">Analysis Workflow *</label>
				<div class="col-md-4">
					<div class="row">
						<div class="col-md-6">
							<select id="analysis-workflow" class="form-control" name="workflow">
								<#list workflows.iterator() as workflow>
								<option value="${workflow.identifier?html}">${workflow.name?html}</option>
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
		<div role="tabpanel">
			<ul class="nav nav-tabs" role="tablist">
			   <li role="presentation"><a href="#target" aria-controls="target" id="target-tab" role="tab" data-toggle="tab">Target</a></li>
			   <li role="presentation" class="active"><a href="#progress" aria-controls="progress" id="progress-tab" role="tab" data-toggle="tab">Progress</a></li>
			 </ul>
			<div class="tab-content">
	    		<div role="tabpanel" class="tab-pane" id="target">
	    			<div class="row" id="analysis-target-select-container">
						<div class="col-md-3">
							<div class="input-group analysis-target-select2">
								<span class="input-group-btn">
									<button id="add-target-btn" class="btn btn-default" type="button" data-select2-open="analysis-target-select">
										<span class="glyphicon glyphicon-plus"></span>
									</button>
								</span>
								<select id="analysis-target-select" class="form-control select2">
								</select>
							</div>
						</div>
		    		</div>
		    		<div class="row">
						<div class="col-md-12">
							<div id="analysis-target-table-container"></div>
							<div id="analysis-target-footer"><span>No target selected. Use the + button to add targets</span></div>
						</div>
		    		</div>
	    		</div>
	    	
	    		<div role="tabpanel" class="tab-pane active" id="progress">
	    			<div id="paper"></div>
	    		</div>
	    	
	    	</div>
    	</div>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<button id="delete-analysis-btn" type="button" class="btn btn-default">Delete</button>
		<button id="clone-analysis-btn" type="button" class="btn btn-default disabled">Clone</button>
		<button id="pause-analysis-btn" type="button" class="btn btn-default hidden">Pause</button>
		<button id="pause-analysis-btn" type="button" class="btn btn-default hidden">Continue</button>
		<button id="run-analysis-btn" type="button" class="btn btn-default">Run</button>
        <button id="rerun-analysis-btn" type="button" class="btn btn-default hidden">ReRun</button>
	</div>
</div>

<div class="modal" id="jobModal" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">	
			<div class="modal-header clearfix">
				<div id="jobPager" class="pull-right"></div>
	        </div>
	      	<div class="modal-body">
	      		<div class="form-horizontal">
  					<div class="form-group">
    					<label for="step" class="col-md-2 control-label">Step:</label>
    					<div class="col-md-4">
      						<input type="text" class="form-control" id="step">
    					</div>
  					</div>
  					<div class="form-group">
    					<label for="Job" class="col-md-2 control-label">Job:</label>
    					<div class="col-md-4">
      						<input type="text" class="form-control" id="job">
    					</div>
  					</div>
  					<div class="form-group">
    					<label for="status" class="col-md-2 control-label">Status:</label>
    					<div class="col-md-4">
      						<input type="text" class="form-control" id="status">
    					</div>
  					</div>
  					<div class="form-group">
    					<label for="message" class="col-md-2 control-label"></label>
    					<div class="col-md-10">
      						<textarea class="col-md-10" id="message"></textarea>
    					</div>
  					</div>
	      		<div>	
			</div>
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
	      	</div>
	    </div>
	</div>
</div>