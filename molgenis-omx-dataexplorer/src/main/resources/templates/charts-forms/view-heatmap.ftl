<div id="chart-designer-modal-heatmap-container">
	<div id="chart-designer-modal-heatmap" class="modal hide" tabindex="-1" role="dialog">
		<div class="modal-header">
	    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	    	<h3>Create Heat map</h3>
	  	</div>
	  	<div class="modal-body">
			<div id="chart-designer-modal-heatmap-form" class="span12">
				<form class="form-horizontal">
					<div class="control-group">
						<label class="control-label" for="heatmap-title">Title</label>
						<div class="controls">
							<input type="text" id="heatmap-title" placeholder="title">
						</div>
					</div>
					
					<div class="control-group">
						<label class="control-label" for="heatmap-select-xaxis-feature">X axis</label>
						<div class="controls">
							<select id="heatmap-select-xaxis-feature" data-placeholder="ObservableValue" name="heatmap-select-xaxis-feature" class="chosen-select"></select>
						</div>
					</div>
					
					<!--
					<div class="control-group">
						<label class="control-label" for="scatterplot-select-yaxis-feature">Y axis</label>
						<div class="controls">
							<select id="scatterplot-select-yaxis-feature" data-placeholder="ObservableValue" name="scatterplot-select-yaxis-feature" class="chosen-select"></select>
						</div>
					</div>
					-->
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<div class="control-group">
				<div class="controls">
					<input id="heatmap-designer-modal-create-button" class="btn" type="button" value="Create Chart" data-dismiss="modal" aria-hidden="true">
				</div>
			</div>
		</div>
	</div>
</div>