(function($, w) {
	"use strict";
	
	var molgenis = w.molgenis = w.molgenis || {};
	var ns = molgenis.form = molgenis.form || {};
	var restApi = new molgenis.RestClient();
	
	ns.onFormSubmit = function() {
		ns.hideAlerts();
		console.log($('#entity-form').serialize());
		
		var action = $('#entity-form').attr('action');
		
		$.ajax({
			type: 'POST',
			url: action,
			data: $('#entity-form').serialize(),
			contentType: 'application/x-www-form-urlencoded',
			async: false,
			success: function(data, textStatus, response) {
				var location = response.getResponseHeader('Location');//Api returns new resource location when creating a new entity
				if (location) {
					var id = restApi.getPrimaryKeyFromHref(location);
					$('#entity-form').attr('action', action + '/' + id);//Create update url, so user can immediately update the created entity by pressing Save 
				}
				$('#success-message').show();
			},
			error: function(request, textStatus, errorThrown) {
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
		
		//If validation succeeds call onFormSubmit
		$.validator.setDefaults({
			submitHandler: function() {
				ns.onFormSubmit();
			}
		});
		
		//Validate occurs on form submit
		$("#entity-form").validate();
		
		$('#success-message .close').on('click', function() {
			$('#success-message').hide();
		});
		
		$('#error-message .close').on('click', function() {
			$('#error-message').hide();
		});
	});
	
}($, window.top));