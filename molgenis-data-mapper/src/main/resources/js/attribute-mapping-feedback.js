(function($, molgenis) {
	"use strict";

	$(function() {
		$('.show-error-message').on('click', function() {
			//$(this).append($(this).data('message'));
			$('#algorithm-error-message-container').html($(this).data('message'));
		});
	});


}($, window.top.molgenis = window.top.molgenis || {}));