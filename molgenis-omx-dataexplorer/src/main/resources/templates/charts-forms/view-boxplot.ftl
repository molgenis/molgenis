<div id="chart-designer-modal-boxplot-container">
	<div id="chart-designer-modal-boxplot" class="modal hide" tabindex="-1" role="dialog">
		<div class="modal-header">
	    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	    	<h3>Create Box Plot</h3>
	  	</div>
	  	<div class="modal-body">
			<div id="chart-designer-modal-boxplot-form" class="span12">
				<form class="form-horizontal">
					<div class="control-group">
						<label class="control-label" for="boxplot-title">Title</label>
						<div class="controls">
							<input type="text" id="boxplot-title" placeholder="title">
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="boxplot-select-feature">Select feature</label>
						<div class="controls">
							<select id="boxplot-select-feature" data-placeholder="ObservableValue" name="chart-select-feature" class="chosen-select"></select>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="boxplot-scale">Outliers scale</label>
						<div class="controls">
							<input type="number" min="0" max="100" step="0.1" id="boxplot-scale" placeholder="1.5">
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="boxplot-select-split-feature">Split to series</label>
						<div class="controls">
							<select id="boxplot-select-split-feature" data-placeholder="ObservableValue" name="boxplot-select-split-feature" class="chosen-select"></select>
						</div>
					</div>
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<div class="control-group">
				<div class="controls">
					<input id="boxplot-designer-modal-create-button" class="btn" type="button" value="Create Chart" data-dismiss="modal" aria-hidden="true">
				</div>
			</div>
		</div>
	</div>
</div>