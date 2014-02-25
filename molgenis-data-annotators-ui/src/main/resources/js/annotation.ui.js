(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	var restApi = new molgenis.RestClient();
	
	molgenis.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		restApi.getAsync(dataSetUri, null, null, function(dataSet) {
			selectedDataSet = dataSet;
			
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
			
			url: molgenis.getContextUrl();
			molgenis.onDataSetSelectionChange($(this).val());		
		});
		
		// fire event handler
		$('#dataset-select').change();
				
		$("#disabled-tooltip").tooltip();
		$("#rootwizard").bootstrapWizard({'tabClass': 'nav nav-tabs'});
			
		// disable the filtering tabs for now
		$("#rootwizard").bootstrapWizard('disable', 1);
		$(".tab2").click(function(){return false;});
		
		$("#rootwizard").bootstrapWizard('disable', 2);
		$(".tab3").click(function(){return false;});
	
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));