var MolgenisMobileConfig = {
	startPage : '#catalogue-page',
	featureCount : 25
};
 
$(document).bind("mobileinit", function() {
	var ns = window.top.molgenis;
	
	//Configure JQM to have a longer list before skipping transitions
	 $.mobile.getMaxScrollForTransition = function () { return 65536; } 

	//Show spinner and overlay when doing a ajax request
	$(document).on({
	    ajaxStart: function() { 
	    	$('body').mask();
			$.mobile.showPageLoadingMsg();
	    },
	    ajaxStop: function() { 
	    	$.mobile.hidePageLoadingMsg(); 
			$('body').unmask();
	    }    
	});
	
	$(document).on('pagebeforeshow', '#login-page', ns.onLoginPageBeforeShow);
	$(document).on('click', '.logout', ns.logout);
});

(function($, w) {
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	
	ns.isUserAuthenticated = function(callback) {
		$.ajax({
			url : '/mobile/authenticated',
			async:false,
			success : function(authenticated) {
				if (authenticated && $.isFunction(callback.authenticated)) {
					callback.authenticated();
				} else if (!authenticated && $.isFunction(callback.unAuthenticated)){
					callback.unAuthenticated();
				}
			}
		});
	}
	
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
		$.ajax({
			type: $(this).attr('method'),
			url: $(this).attr('action'),
			data: $(this).serialize(),
			async:false,
			success : function(response) {
				if (response.success) {
					$.mobile.changePage(MolgenisMobileConfig.startPage, {transition: "flip"});
				} else {
					alert(response.errorMessage);
				}
			}
		});
	}
	
	ns.logout = function() {
		$.ajax({
			type: 'POST',
			url: '/logout',
			success : function() {
				$('#features').html('').listview('refresh');
				$.mobile.changePage("#login-page", {transition: "flip", reverse: true});
			}
		});
	}
}($, window.top));