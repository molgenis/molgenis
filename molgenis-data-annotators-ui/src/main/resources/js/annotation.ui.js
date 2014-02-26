(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	var restApi = new molgenis.RestClient();
	
	molgenis.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		restApi.getAsync(dataSetUri, null, null, function(dataSet) {
			selectedDataSet = dataSet;
			alert(selectedDataSet.name);
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