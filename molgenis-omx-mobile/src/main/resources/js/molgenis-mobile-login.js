var MolgenisMobileConfig = {
		startPage : '#catalogue-page',
		featureCount : 10
};

$(document).bind("mobileinit", function() {
	var ns = window.top.molgenis;
	
	$(document).on('pagebeforeshow', '#login-page', ns.onLoginPageBeforeShow);
	$(document).on('click', '.logout', ns.logout);
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
	
	ns.logout = function() {
		$.mobile.showPageLoadingMsg(); 
		$.ajax({
			url : '/mobile/logout',
			success : function() {
				$.mobile.changePage("#login-page", {transition: "flip", reverse: true});
			}
		});
	}
}($, window.top));