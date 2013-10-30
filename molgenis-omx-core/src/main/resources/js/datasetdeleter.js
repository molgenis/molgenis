(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var restApi = new ns.RestClient();
	
	// fill dataset select
	ns.fillDataSetSelect = function(callback) {
		var maxNrOfDataSets = 500;
		
		restApi.getAsync('/api/v1/dataset', null, {num: maxNrOfDataSets}, function(datasets) {
			var items = [];
			
			$.each(datasets.items, function(key, val) {
				items.push('<option value="' + val.identifier + '">' + val.name + '</option>');
			});
			$('#dataset-select').html(items.join(''));
		});
	};

	ns.deleteDataSet = function(e){	
		parent.showSpinner();
		e.preventDefault();
		e.stopPropagation();
		var form = $('#deletedataset-form');
		$.ajax({
		    type: 'POST',
		    url: molgenis.contextUrl + '/delete',
		    data: form.serialize(),
		    success: function (msg) {
		    	$('#plugin-container').before($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button>Dataset ' + msg + ' was successfully removed</div>'));
		    	ns.fillDataSetSelect();
		    	parent.hideSpinner();
		    },
		    error:function (xhr, ajaxOptions, thrownError){ 
		    	parent.hideSpinner();
		    	$('#plugin-container').before($('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button>An error occurred while deleting the dataset</div>'));  
		 } 
	});
};
}($, window.top.molgenis = window.top.molgenis || {}));
