<div id="annotator-select-container">
	<form id="annotate-dataset-form" role="form">
		<div class="form-group">
			<label class="control-label" for="annotator-checkboxes-enabled"><h5>Annotators available</h5></label>
			<div id="annotator-checkboxes-enabled" class="controls"></div>
			<hr></hr>
			<label class="control-label" for="annotator-checkboxes-disabled">
				<h5>Annotations not available
					<a id="disabled-tooltip" data-toggle="tooltip"
						title= "These annotations are not available for the selected data set because: 
						1) The annotation data is not available on the server, 2) A webservice might be offline or 3) Your data set does not contain the correct columns"> 
							
						<span class="icon icon-question-sign"></span>
					</a>
				</h5> 
			</label>
			<div id="annotator-checkboxes-disabled" class="controls"></div>
			<hr></hr>
			<div id="row-fluid">
				<div id="span8">
					<input type="hidden" value="" id="dataset-identifier" name="dataset-identifier">
					<button id="annotate-dataset-button" class="btn">Run annotation</button>
					<input type="checkbox" name="createCopy"> Copy before annotating
				</div>
			</div>
		</div>		
	</form>
</div>
<script>
	$.when($.ajax("/js/dataexplorer-annotators.js", {'cache': true}))
		.then(function() {
			molgenis.dataexplorer.annotators.getAnnotatorSelectBoxes();		
		});
</script>