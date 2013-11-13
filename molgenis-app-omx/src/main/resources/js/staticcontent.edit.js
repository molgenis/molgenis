(function($, molgenis) {
	"use strict";
	
	$(function() {
		tinymce.init({
		    selector: "textarea#elm1",
		    plugins: [
		        "advlist autolink lists link image charmap print preview anchor",
		        "searchreplace visualblocks code fullscreen",
		        "insertdatetime media table contextmenu paste"
		    ],
		    toolbar: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image"
		});
		
		$('#submitBtn').click(function(){
			$("#contentForm").submit();
		});
	});

}($, window.top.molgenis = window.top.molgenis || {}));
