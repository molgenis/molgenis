(function($, molgenis) {
	"use strict";

	$(function() {
		var styleName;

		function updatePageTheme() {
			styleName = $('#bootstrap-theme-select').find(":selected").text();
			var cssLocation = $('#bootstrap-theme-select').val();
			var link = $('<link />').attr('id', 'bootstrap-theme').attr('rel', 'stylesheet').attr('type', 'text/css');

			$(link).attr('href', "/css/" + cssLocation);

			$('#bootstrap-theme').remove(); // Remove existing preview theme
			$('head').append(link); // Set new preview theme
		}
		
		$('#bootstrap-theme-select').on('change', updatePageTheme);

		$('#save-selected-bootstrap-theme').on('click', function(event) {
			event.preventDefault();
			updatePageTheme();
			$.ajax({
				contentType : 'application/json',
				type : 'POST',
				url : molgenis.getContextUrl() + '/set-bootstrap-theme',
				data : '"' + styleName + '"',
				success : function(succes) {
					molgenis.createAlert([ {
						'message' : 'Succesfully updated the application theme'
					} ], 'success');
				}
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));