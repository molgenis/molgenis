(function($, molgenis) {
	"use strict";
	
	var self = molgenis.datasetdeleter = molgenis.datasetdeleter || {};
	var restApi = new molgenis.RestClient();
	
	// fill dataset select
	self.fillDataSetSelect = function() {
		var maxNrOfDataSets = 500;
		
		restApi.getAsync('/api/v1/dataset', {'q': {'num': maxNrOfDataSets}}, function(datasets) {
			var items = [];
			
			$.each(datasets.items, function(key, val) {
				items.push('<option value="' + val.Identifier + '">' + val.Name + '</option>');
			});
			$('#dataset-select').html(items.join(''));
			$('#dataset-select').select2({ width: 'resolve' });
		});
	};

	self.deleteDataSet = function(e){	
		e.preventDefault();
		e.stopPropagation();
		var form = $('#deletedataset-form');
		$.ajax({
		    type: 'POST',
		    url: molgenis.getContextUrl() + '/delete',
		    data: form.serialize(),
		    success: function (msg) {
		    	molgenis.createAlert([{'message': 'Dataset ' + msg + ' was successfully removed'}], 'success');
		    	self.fillDataSetSelect();
		    } 
		 }); 
	};
	
	$(function() {
		self.fillDataSetSelect();
		
		var submitBtn = $('#delete-button');
		var form = $('#deletedataset-form');
		form.submit(function(e){
			self.deleteDataSet(e);
		});
		
		submitBtn.click(function(e) {
			e.preventDefault();
			e.stopPropagation();
			form.submit();
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
