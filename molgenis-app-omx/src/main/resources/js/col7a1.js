(function($, molgenis) {
	"use strict";
	
	function createTable(){
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + '/create',
			contentType : 'application/json',
			success : function(response) {
				$('#table-container').html(response);
			},
			error : function(response) {
				molgenis.createAlert([{'message': 'View cannot be created'}], 'error');
			},
		});
	}
	
	$(function() {
		$('#refresh_button').click(function(){
			$.ajax({
				type : 'GET',
				url : molgenis.getContextUrl() + '/generate',
				contentType : 'application/json',
				success : function(response) {
					if(response === true) {
						createTable();
						molgenis.createAlert([{'message': 'View generated'}], 'success');
					}else{
						molgenis.createAlert([{'message': "View cannot be generated"}], 'error');
					}
				}
			});
		});
		
		createTable();
	});
}($, window.top.molgenis = window.top.molgenis || {}));
