(function($, molgenis) {
	"use strict";
	
	$(function() {
		var data = $('#form-holder').data();
		getForm(data.name, data.id);
		
		$('#submit-button').click(function(e) {
			$('#entity-form input[name=status]').val('SUBMITTED');
			$('#entity-form').submit();
			document.location = data.name + "/thanks";
		});
	});
	
	function getForm(name, id) {
		$.ajax({
			type : 'GET',
			url : '/api/v1/' + name + '/' + id + '/edit',
			success : function(text) {
				$('#form-holder').html(text);	
			}
		});
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));