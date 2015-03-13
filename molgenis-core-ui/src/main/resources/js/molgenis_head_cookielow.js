(function($, molgenis) {
	"use strict";
	
	$(function() {
		var cookieValue = $.cookie("toestemmingvoorcookies");
		
		if(undefined === cookieValue){
			$('.navbar.navbar-default.navbar-fixed-top').prepend(
					$('<div id="accept-cookies-container" class="container-fluid">' +
							'<div class="jumbotron">' +
								'<p class="text-center">' + window.location.hostname + ' uses cookies to analyze the use of the site and improve usability. Read more about <a href="/cookies/">cookies</a>.</p>' +
								'<p class="text-center"><a id="accept-cookies" class="btn btn-primary btn-lg" href="#" role="button">Accept cookies</a></p>' + 
							'</div>' + 
						'</div>'
						));
		
			$('body').css({'margin-top': $('#accept-cookies-container').height()});
			
			$('#accept-cookies').on('click', function(){
				$.cookie("toestemmingvoorcookies", "true", {expires:365, path:'/', secure:false});
				$('#accept-cookies-container').fadeOut(1000);
				
				// Reset body margin-top default value
				setTimeout(function(){$('body').css({'margin-top': 0});}, 1000);
			});
		}
	});

}($, window.top.molgenis = window.top.molgenis || {}));
