var MolgenisMobileConfig = {
		startPage : '#catalogue-page'
};

$(document).bind("mobileinit", function() {
	$(document).on('pagebeforeshow', '#login-page', window.top.molgenis.onLoginPageBeforeShow);

});

(function($, w) {
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	
	ns.onLoginPageBeforeShow = function() {
		$('#username').val('');
		$('#password').val('');
		
		//Don't submit the page but call out ajax login method
		$.validator.setDefaults({
			submitHandler: function() {
				ns.login();
			}
		});
		
		//Validate occurs on form submit
		$("#login-form").validate({
			rules: {
				username: "required",
				password: "required"
			},
			messages: {
				username: "Please enter your username",
				password: "Please enter your password",
			},
			errorPlacement: function(error, element) {
				error.insertAfter($(element).parent());	
			}
		});
	}
	
	ns.login = function() {	
		$.mobile.showPageLoadingMsg(); 
		$.ajax({
			type : 'POST',
			url : '/mobile/login',
			data : JSON.stringify({
				username: $('#username').val(),
				password: $('#password').val()
			}),
			contentType : 'application/json',
			success : function(response) {
				if (response.success) {
					$.mobile.changePage(MolgenisMobileConfig.startPage, {transition: "flip"});
				} else {
					$.mobile.hidePageLoadingMsg(); 
					alert(response.errorMessage);
				}
			}
		});
	}
}($, window.top));