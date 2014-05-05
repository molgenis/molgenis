(function($, molgenis) {
	"use strict";
	
	$(function() {
		tinymce.init({
		    selector: "textarea#elm1",
		    theme: "modern",
		    plugins: [
		        "advlist autolink lists link charmap print preview anchor",
		        "searchreplace visualblocks code fullscreen",
		        "insertdatetime table contextmenu paste"
		    ],
		    toolbar: "insertfile undo redo | styleselect fontselect fontsizeselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link",
		    setup : function(ed) {
	            ed.on('change', function(e) {
	            	$('#submitBtn').removeProp('disabled');
	            });
            }
		});
		
    	$('#submitBtn').prop('disabled', true);
	});

}($, window.top.molgenis = window.top.molgenis || {}));
