(function($, molgenis) {
	"use strict";
	
	var ns = molgenis.form = molgenis.form || {};
	var restApi = new molgenis.RestClient();
	var successHandler;
	var meta;
	
	ns.quoteIsoDateT = function() {
		$('.datetime input').each(function() {
			var d = $(this).val();
			$(this).val(d.replace(/T/,"'T'"));//Put quotes around T
		});
	};
	
	ns.onFormSubmit = function() {
		ns.hideAlerts();
		
		$('.datetime input').each(function() {
			var d = $(this).val();
			$(this).val(d.replace(/'/g,''));//Remove quotes from isodateformat
		});
		
		var action = $('#entity-form').attr('action');
		
		$.ajax({
			type: 'POST',
			url: action,
			data: $('#entity-form').serialize(),
			contentType: 'application/x-www-form-urlencoded',
			async: false,
			success: function(data, textStatus, response) {
			
				//Add quotes to dateformat again
				ns.quoteIsoDateT();
				
				var location = response.getResponseHeader('Location');//Api returns new resource location when creating a new entity
				
				if (location) {
					$('#entity-form').attr('action', location);//Create update url, so user can immediately update the created entity by pressing Save 
					$('input[name=_method]').val('PUT');
				}
				$('#success-message').show();
				
				$(document).trigger('onFormSubmitSuccess');
			},
			error: function(xhr) {
				ns.quoteIsoDateT();
				
				var messages = [];
				$.each(JSON.parse(xhr.responseText).errors, function(index, error) {
					messages.push(error.message);
				});
				$('#error-message-details').html('Details: ' + messages.join('\n'));
				$('#error-message').show();
			}
		});
		
	};
	
	ns.hideAlerts = function() {
		$('#success-message').hide();
		$('#error-message').hide();
	};
	
	ns.updateInputsVisibility = function() {	
		var e = {};
		$.each($('#entity-form').serializeArray(), function(index, input) {	
			e[input.name] = input.value;
		});
		
		$.each(meta.attributes, function(name, attr) {
			if (attr.visibleExpression) {
				var visible = evalScript(attr.visibleExpression, e);
				if (visible === true) {
					$('#entity-form input[name=' + attr.name + "]").closest('.form-group').show();
				} else if (visible === false) {
					$('#entity-form input[name=' + attr.name + "]").closest('.form-group').hide();
				}
			}
		});
	};
	
	$(function() {
		meta = restApi.get('/api/v1/' + $('#entity-form').data('id') + '/meta?expand=attributes');
		
		setTimeout(function(){ ns.updateInputsVisibility(); }, 10);//TODO get rid of setTimeout

		//Enable datepickers
		$('.date.datetime').datetimepicker({pickTime: true, useSeconds : true});
		$('.date.dateonly').datetimepicker({pickTime: false, useSeconds : false});
		
		// Closes other datetime pickers when opening another one
		$('.date').on('dp.show', function() {
	        $('.date').not($(this)).each(function() {
	            $(this).data("DateTimePicker").hide();
	        });
	    });
		
		//If validation succeeds call onFormSubmit
		$.validator.setDefaults({
			submitHandler: function() {
				ns.onFormSubmit();
			}
		});

		//Validate occurs on form submit
		$("#entity-form").validate({
			ignore: null, //Needed for validation of xref,mref.  To validate hidden fields
			rules: remoteRules,
			messages: remoteMessages
		});
		
		$('#success-message .close').on('click', function() {
			$('#success-message').hide();
		});
		
		$('#error-message .close').on('click', function() {
			$('#error-message').hide();
		});
		
		
		$('.empty-date-input').on('click', function() {
			$(this).parent().parent().children('input').val('');
		});
		
		$('input').on('change', function() {
			ns.updateInputsVisibility();
		});
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));