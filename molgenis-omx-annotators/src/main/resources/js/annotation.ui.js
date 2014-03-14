(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	
	var restApi = new molgenis.RestClient();
	
	molgenis.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		restApi.getAsync(dataSetUri, null, function(dataSet) {
			selectedDataSet = dataSet;
			
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/change-selected-dataset',
				data : JSON.stringify(selectedDataSet.Identifier),
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
					
					$('#annotator-checkboxes-enabled').html(enabledAnnotators.join(""));
					$('#annotator-checkboxes-disabled').html(disabledAnnotators.join(""));
					$('#selected-dataset-name').html(selectedDataSet.Name);
					$('#dataset-identifier').val(selectedDataSet.Identifier);
				}
			});
		});
	};

	// on document ready
	$(function() {
		$('#dataset-select').chosen();
		$('#dataset-select').change(function() {
			if($("#dataset-select").val() != null){
				
				restApi.getAsync($('#dataset-select').val(), null, function(dataSet) {
					selectedDataSet = dataSet;
				});
			
				molgenis.onDataSetSelectionChange($(this).val());
			}	
		});
		
		// fire event handler
		$('#dataset-select').change();
			
		$("#disabled-tooltip").tooltip();
		$("#remove-button-selected-phenotype").tooltip();
		
		$("#rootwizard").bootstrapWizard({'tabClass': 'nav nav-tabs'});
		$("#phenotypeSelect").chosen();	
		
		// disable the filtering tabs
		$("#rootwizard").bootstrapWizard('disable', 1);
		$(".tab2").click(function(){return false;});
		
		$("#rootwizard").bootstrapWizard('disable', 2);
		$(".tab3").click(function(){return false;});
	
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));	