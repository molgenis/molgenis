<#include "resource-macros.ftl">
<div class="row">
	<div class="col-md-12" id="annotator-select-container">
		<form id="annotate-dataset-form" role="form" class="well">
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						<legend>Annotators available</legend>
						<div class="row">
							<div class="col-md-12">							
								<a href="#" class="btn btn-link pull-left select-all-btn">Select all</a>
								<a href="#" class="btn btn-link pull-left deselect-all-btn">Deselect all</a>
							</div>
						</div>
						
						<div id="enabled-annotator-selection-container"></div>
					</div>
				</div>
				
				<div class="col-md-6">
					<div class="form-group">
						<legend>Annotations not available
					    	<a id="disabled-tooltip" data-toggle="tooltip"
					        	title= "These annotations are not available for the selected data set because:
					            1) The annotation data is not available on the server, 2) A webservice might be offline or 3) Your data set does not contain the correct columns">
					            <span class="glyphicon glyphicon-question-sign"></span>
					        </a>
    					</legend>
    					
    					<div id="disabled-annotator-selection-container"></div>
    				</div>
				</div>
        	</div>

        	<div class="row">
        		<div class="col-md-12">	
        			<hr></hr>
	        		<div class="form-group">
						<input type="hidden" value="" id="dataset-identifier" name="dataset-identifier">
						<button id="annotate-dataset-button" class="btn btn-default">Run annotation</button>
					</div>
	
		            <input type="checkbox" name="createCopy"> Copy before annotating
		    
	            </div>
            </div>
    	</form>
    </div>
</div>

<script id="annotator-template" type="text/x-handlebars-template">
	{{#equal this.enabled 'true'}}
		<div class="checkbox">
			<label>
				<input type="checkbox" class="checkbox" name="annotatorNames" value="{{this.annotatorName}}">{{this.annotatorName}}
				<a id="disabled-tooltip" class="darktooltip" data-toggle="tooltip" title="Input: {{this.inputMetaData}} Output: {{this.outputMetaData}}">
					<span class="glyphicon glyphicon-info-sign"></span></span>
				</a>
			</label>
		</div>
	{{/equal}}

	{{#notequal this.enabled 'true'}}
		<label class="checkbox">
			{{this.annotatorName}}<a id="disabled-tooltip" class="darktooltip" data-toggle="tooltip" title="Input: {{this.inputMetaData}} Output: {{this.outputMetaData}}">
			<span class="glyphicon glyphicon-info-sign"></span></a>
		</label>
	{{/notequal}}
</script>

<script>
	$.when($.ajax("<@resource_href "/js/dataexplorer-annotators.js"/>", {'cache': true}))
		.then(function() {
			molgenis.dataexplorer.annotators.getAnnotatorSelectBoxes();		
		});

    $('.select-all-btn').click(function(e) {
        $("input[name='annotatorNames']").each(function() {
            this.checked = true;
        });
    });

    $('.deselect-all-btn').click(function(e) {
        $("input[name='annotatorNames']").each(function() {
            this.checked = false;
        });
    });
</script>