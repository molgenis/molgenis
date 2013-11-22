(function($, molgenis) {
	"use strict";

	$(document).ready(function(){
		//TODO JJ
		function setStatusMessage() {
			alert("lala");
			var url = "/menu/admin/dataindexer/status";
		    $.get(url, function() {
		    	alert("TEST");
////		    	console.log(url);
////				molgenis.createAlert([{'message': response.message}], response.isRunning ? 'success' : 'error');
////				alert('response.isRunning');
////				if(response.isRunning === true){
////					setTimeout(setStatusMessage(), 1000);
////					alert("setTimeout()");
//				}
			});
		};
		
		setStatusMessage();
	});
})($, window.top.molgenis = window.top.molgenis || {});
