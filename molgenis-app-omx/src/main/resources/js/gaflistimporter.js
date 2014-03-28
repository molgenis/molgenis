(function($, molgenis) {
	"use strict";
	
	$(function() {
		$('#gaflist-import-form').submit(function(e) {
			e.preventDefault();
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				data : $(this).serialize(),
				success : function() {
					molgenis.createAlert([{'message': 'GAF list imported'}], 'success');
				}
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
