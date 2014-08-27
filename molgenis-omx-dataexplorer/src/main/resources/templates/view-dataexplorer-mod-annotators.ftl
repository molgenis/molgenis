<#include "resource-macros.ftl">
<div class="row">
        <div class="col-md-12">
            <div id="annotator-select-container">
            	<form id="annotate-dataset-form" role="form">
                    <div class="well">
                    <legend>Annotators available</legend>
                    <div id="annotator-checkboxes-enabled"></div>
            		<legend>Annotations not available
                                <a id="disabled-tooltip" data-toggle="tooltip"
                                    title= "These annotations are not available for the selected data set because: 
                                    1) The annotation data is not available on the server, 2) A webservice might be offline or 3) Your data set does not contain the correct columns"> 
                                    <span class="glyphicon glyphicon-question-sign"></span>
                                </a>
                    </legend>
                    <div id="annotator-checkboxes-disabled"></div>
            		</div>
					<input type="hidden" value="" id="dataset-identifier" name="dataset-identifier">
					<button id="annotate-dataset-button" class="btn btn-default">Run annotation</button>
					<input type="checkbox" name="createCopy"> Copy before annotating	
            	</form>
            </div>
    </div>
</div>
<script>
	$.when($.ajax("<@resource_href "/js/dataexplorer-annotators.js"/>", {'cache': true}))
		.then(function() {
			molgenis.dataexplorer.annotators.getAnnotatorSelectBoxes();		
		});
</script>