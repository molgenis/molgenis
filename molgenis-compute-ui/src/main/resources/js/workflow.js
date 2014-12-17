$(function() {
	$("#targetType").select2();
	
	$('#backButton').on('click', function() {
		var url = window.location.href;
		url = url.substr(0, url.indexOf('/workflow/') + 9);
		window.location.href = url;
	});
	
	$('#submitFormButton').on('click', function() {
		var form = $('#workflowForm');
		if (form.valid()) {
			showSpinner(function() {
				form.submit();
			});
		}
	});
	
	
});