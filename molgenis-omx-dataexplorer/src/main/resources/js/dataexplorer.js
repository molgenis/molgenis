(function ($, w) {
    "use strict";
    
    var ns = w.molgenis = w.molgenis || {};
    
    ns.selectDataSet = function(id) {
    	console.log("selectDataSet: " + id);
    	$.getJSON('/api/v1/dataset?' + id, function(data) {
			ns.createFeatureSelection(data);
			ns.createObservationSetsTable(data);
		});
    };
    
    ns.searchObservationSets = function(query) {
    	console.log("searchObservationSets: " + query);
    	console.log("TODO execute search query");
    	ns.updateTableObservationSets("<observation sets and nr rows/cols>");
    };
    
    ns.createFeatureSelection = function(dataset) {
    	console.log("createFeatureSelection: " + dataset);
    };
    
    ns.createObservationSetsTable = function(dataset) {
    	console.log("createObservationSetsTable: " + dataset);
    	console.log("  update table");
    	console.log("  update table pager");
    	console.log("  update search results header");
    };
    
    ns.updateTableObservationSets = function(observationSets) {
    	console.log("updateObservationSets: " + observationSets);
    	console.log("  update table");
    	console.log("  update table pager");
    	console.log("  update search results header");
    };
    
    ns.updateTableFeatures = function(features) {
    	console.log("updateTableFeatures: " + features);
    };
    
    ns.downloadExcel = function(filteredDataset) {
    	console.log("downloadExcel: " + filteredDataset);
    	//TODO need server controller to handle download
    }
    
    ns.createFeatureFilter = function() {
    	console.log("createFeatureFilter: ");
    	
    	// create new filter
    	// add to feature filters
    	// add click callback for delete 
    }
    
    ns.updateFeatureFilter = function() {
    	console.log("updateFeatureFilter: ");
    }
    
    ns.removeFeatureFilter = function() {
    	console.log("removeFeatureFilter: ");
    }
    
    $(function() {
    	$("#dataset-chooser").chosen();
    	$("#feature-filters-accordion").accordion({ collapsible: true, active: false });
    	$("#feature-selection-accordion").accordion({ collapsible: true });
    	$("#observationset-search").submit(function(e){
			e.preventDefault();
			ns.searchObservationSets($('#observationset-search input').val());
		});
    	$("#download-button").click(function(e) {
    		e.preventDefault();
    		ns.downloadExcel("<filteredDataset>");
    	});
    	$(".edit-filter").click(function(e) {
    		e.preventDefault();
    		e.stopPropagation();
    		$('<div></div>').dialog();
    		ns.updateFeatureFilter();
    	});
    	$(".remove-filter").click(function(e) {
    		e.preventDefault();
    		ns.removeFeatureFilter();
    	});
    	$(".select-feature-checkbox").click(function() {
    		var check = $(this).attr('checked') ? true : false;
    		console.log("feature enabled? " + check);
    		ns.updateTableFeatures("feature:");
    	});
    	$(".select-all-features-checkbox").click(function() {
    		$(this).parents('div.protocol-feature-selection').find('.select-feature-checkbox').click();
    	});
    	// trigger select of first data set
    	molgenis.selectDataSet($("#dataset-chooser").find(":selected").val());
    });
}($, window));