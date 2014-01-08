<div id="chart-designer-modal-scatterplot-container">
	<div id="chart-designer-modal-scatterplot" class="modal hide" tabindex="-1" role="dialog">
		<div class="modal-header">
	    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	    	<h3>Create Scatter Plot</h3>
	  	</div>
	  	<div class="modal-body">
			<div id="chart-designer-modal-scatterplot-form" class="span12">
				<form class="form-horizontal">
					<div class="control-group">
						<label class="control-label" for="scatterplot-title">Title</label>
						<div class="controls">
							<input type="text" id="scatterplot-title" placeholder="title">
						</div>
					</div>
					
					<!-- TODO adding the functionality to make line, spline or scatter plot
					<div class="control-group">
						<label class="control-label" for="chart-select-type">type</label>
						<div class="controls">
							<select id="chart-select-type" data-placeholder="type" name="chart-select-type" class="chosen-select">
								<option value="LINE_CHART">line</option>
								<option value="SPLINE_CHART">spline</option>
								<option value="SCATTER_CHART">scatter</option>
							</select>
						</div>
					</div>
					-->
					
					<div class="control-group">
						<label class="control-label" for="scatterplot-select-xaxis-feature">X axis</label>
						<div class="controls">
							<select id="scatterplot-select-xaxis-feature" data-placeholder="ObservableValue" name="chart-select-xaxis-feature" class="chosen-select"></select>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="scatterplot-select-yaxis-feature">Y axis</label>
						<div class="controls">
							<select id="scatterplot-select-yaxis-feature" data-placeholder="ObservableValue" name="scatterplot-select-yaxis-feature" class="chosen-select"></select>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="scatterplot-select-split-feature">Split to series</label>
						<div class="controls">
							<select id="scatterplot-select-split-feature" data-placeholder="ObservableValue" name="scatterplot-select-split-feature" class="chosen-select"></select>
						</div>
					</div>
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<div class="control-group">
				<div class="controls">
					<input id="scatterplot-designer-modal-create-button" class="btn" type="button" value="Create Chart" data-dismiss="modal" aria-hidden="true">
				</div>
			</div>
		</div>
	</div>
</div>