(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	var selectedEntity;
	var selectedEntityName;
	
	$(function() {
		
		$('.entity-dropdown-item').click(function() {
			var entityUri = $(this).attr('id');
			load(entityUri);
		});

		/**$('#term-input').on('keyup paste', null, function(){
			$('#term-input').dropdown('toggle');
			$.get(molgenis.getContextUrl() + '/ac',
				{search:$('#term-input').val()},
				function(data){
				$('#ac-menu').html(data);
			});
		});*/
		
		$('#filter-submit').on('click', null, function() {
			$.post(molgenis.getContextUrl() + '/filter',
			{terms:$('#term-input').val(), entity:selectedEntity.label, recursive:true},
			function(data){
				console.log(data);
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
		$('#dropdown-menu-entities').html(entityMetaData.label+" <span class=\"caret\"></span>");
	}
}($, window.top.molgenis = window.top.molgenis || {}));