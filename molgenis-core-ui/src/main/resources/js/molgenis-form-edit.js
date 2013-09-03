(function($, w) {
	"use strict";
	
	var molgenis = w.molgenis = w.molgenis || {};
	var ns = molgenis.form = molgenis.form || {};
	var restApi = new molgenis.RestClient();
	
	ns.onFormSubmit = function() {
		ns.hideAlerts();
		console.log($('#entity-form').serialize());
		
		$.ajax({
			type: 'POST',
			url: $('#entity-form').attr('action'),
			data: $('#entity-form').serialize(),
			contentType: 'application/x-www-form-urlencoded',
			async: false,
			success: function() {
				$('#success-message').show();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				$('#error-message-content').html(errorThrown);
				$('#error-message').show();
			}
		});
	}
	
	ns.hideAlerts = function() {
		$('#success-message').hide();
		$('#error-message').hide();
	}
	
	$(function() {
		$('#entity-form').on('submit', function() {
			ns.onFormSubmit();
			return false;
		});
	});
	
}($, window.top));