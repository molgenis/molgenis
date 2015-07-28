$(function() {
	$('ul.pager a').on('click', function(e) {
		e.preventDefault();
		
		if (!$(this).parent().hasClass('disabled')) {
			showSpinner();
			var form = $('#wizardForm');
			
			form.attr('action', $(this).attr('href'));
			form.submit();
		}
		
		return false;
	});
});