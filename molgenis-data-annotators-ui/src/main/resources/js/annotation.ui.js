(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	var restApi = new molgenis.RestClient();
	
	molgenis.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		restApi.getAsync(dataSetUri, null, null, function(dataSet) {
			selectedDataSet = dataSet;
			
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/changeSelectedDataSet',
				data : JSON.stringify(selectedDataSet.identifier),
				contentType : 'application/json',
				success : function(resultMap) {
					var enabledAnnotators = [];
					var disabledAnnotators = [];
					
					for(var key in resultMap){
						if(resultMap[key] == true){
							enabledAnnotators.push('<label class="checkbox">\n');
							enabledAnnotators.push('\t<input type="checkbox" class="checkbox" name="annotatorNames" value="' + key + '">' + key);
							enabledAnnotators.push('</label>');
							
						}else{
							disabledAnnotators.push('<label class="checkbox">\n');
							disabledAnnotators.push('\t<input type="checkbox" class="checkbox" name="annotatorNames" disabled value="' + key + '">' + key);
							disabledAnnotators.push('</label>');
							
						}
					}
					
					if(enabledAnnotators.length > 0){
						enabledAnnotators.push('<hr></hr>');
					}
					
					$('#annotator-checkboxes-enabled').html(enabledAnnotators.join(""));
					$('#annotator-checkboxes-disabled').html(disabledAnnotators.join(""));
				}
			});
		});
	};

	// on document ready
	$(function() {
		// use chosen plugin for data set select
		$('#dataset-select').chosen();
		$('#dataset-select').change(function() {
			restApi.getAsync($('#dataset-select').val(), null, null, function(dataSet) {
				selectedDataSet = dataSet;
			});
			
			molgenis.onDataSetSelectionChange($(this).val());
			
		});
		
		// fire event handler
		$('#dataset-select').change();
			
		$('#createDataSet').click(function() {
			var inputFile = $('#file-input-field').val();
			url:  molgenis.getContextUrl() + '/create-new-dataset-from-tsv';
		});
		
		$("#disabled-tooltip").tooltip();
		$("#rootwizard").bootstrapWizard({'tabClass': 'nav nav-tabs'});
			
		// disable the filtering tabs for now
		$("#rootwizard").bootstrapWizard('disable', 1);
		$(".tab2").click(function(){return false;});
		
		$("#rootwizard").bootstrapWizard('disable', 2);
		$(".tab3").click(function(){return false;});
	
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));	