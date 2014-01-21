(function($, molgenis) {
	"use strict";
	
	$(function() {
		$('#gaflist-import-form').submit(function(e) {
			e.preventDefault();
			showSpinner();
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				data : $(this).serialize(),
				success : function() {
					hideSpinner();
					molgenis.createAlert([{'message': 'GAF list imported'}], 'success');
				},
				error : function (xhr, textStatus, errorThrown) {
					hideSpinner();
					molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				}
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
