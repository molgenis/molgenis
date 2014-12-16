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
			   <li role="presentation" class="active"><a href="#target" aria-controls="target" role="tab" data-toggle="tab">Target</a></li>
			</ul>
			<div class="tab-content">
	    		<div role="tabpanel" class="tab-pane active" id="target">
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
						</div>
		    		</div>
	    		</div>
	    	</div>
    	</div>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<button id="delete-analysis-btn" type="button" class="btn btn-default">Delete</button>
		<button id="clone-analysis-btn" type="button" class="btn btn-default">Clone</button>	
		<button id="run-analysis-btn" type="button" class="btn btn-default">Run</button>
	</div>
</div>