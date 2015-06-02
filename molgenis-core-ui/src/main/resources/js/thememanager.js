(function($, molgenis) {
	"use strict";

	$(function() {
		var styleName;

		$('#bootstrap-theme-select').on('change', function() {
			styleName = $(this).find(":selected").text();

			var cssLocation = $(this).val();
			var link = $('<link />').attr('id', 'bootstrap-theme').attr('rel', 'stylesheet').attr('type', 'text/css');

			$(link).attr('href', "/css/themes/" + cssLocation);

			$('#bootstrap-theme').remove(); // Remove existing preview theme
			$('head').append(link); // Set new preview theme
		});

		$('#save-selected-bootstrap-theme').on('click', function(event) {
			event.preventDefault();
			var selectedBootstrapTheme = $('#bootstrap-theme-select').find(":selected").text();
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