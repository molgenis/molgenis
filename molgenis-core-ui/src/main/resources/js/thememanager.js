(function($, molgenis) {
	"use strict";

	$(function() {
		var styleName;

		$('#bootstrap-theme-select').on('change', function() {
			// Set selected style name to use in ajax post
			styleName = $(this).find(":selected").text();
			
			var cssLocation = $(this).val();
			var link = $('<link />').attr('id', 'bootstrap-theme').attr('rel', 'stylesheet').attr('type', 'text/css');

			if (cssLocation.indexOf("//bootswatch") === 0) {
				$(link).attr('href', cssLocation);
			} else {
				$(link).attr('href', '/css/themes/' + cssLocation);
			}

			$('#bootstrap-theme').remove();
			$('head').append(link);
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
						'message' : 'Succesfully updated the molgenis bootstrap theme'
					} ], 'success');
				}
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));