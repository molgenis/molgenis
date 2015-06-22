(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	
	$(function() {
		var selectedEntityName;
		
		$('.entity-dropdown-item').click(function() {
			var entityUri = $(this).attr('id');
			load(entityUri);
		});
		console.log(molgenis.getContextUrl() + '/ac');
		$('#term-input').on('keyup paste', null, function(){
			$.get(molgenis.getContextUrl() + '/ac',
				{search:$('#term-input').val()},
				function(data){
				$('#ac-menu').html(data);
			});
		});
	});
		
	if (selectedEntityName) {
		load('/api/v1/' + selectedEntityName);
	}
	
	function load(entityUri) {
		restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
			selectedEntity = entityMetaData;
			createHeader(entityMetaData);
		});
	}
	
	function createHeader(entityMetaData) {
		$('#filter-title').html("Filtering '"+entityMetaData.label+"' by HPO");
	}
}($, window.top.molgenis = window.top.molgenis || {}));