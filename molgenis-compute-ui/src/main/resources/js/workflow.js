$(function() {
	$('#backButton').on('click', function() {
		var url = window.location.href;
		url = url.substr(0, url.indexOf('/workflow/') + 9);
		window.location.href = url;
	});
});